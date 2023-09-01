/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.commands.hivemq.export.clients;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.LoggerUtils;
import com.opencsv.CSVWriter;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

@CommandLine.Command(name = "clients",
                     description = "Export HiveMQ client details",
                     sortOptions = false,
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class ExportClientsCommand implements Callable<Integer> {

    public enum OutputFormat {
        csv
    }

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-url"},
                        defaultValue = "http://localhost:8888",
                        description = "The URL of the HiveMQ REST API endpoint (default http://localhost:8888)",
                        order = 1)
    private @NotNull String url;

    @CommandLine.Option(names = {"-f", "--file"},
                        description = "The file to write the output to (defaults to a timestamped file in the current working directory)",
                        order = 2)
    private @Nullable File file;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--rate"},
                        defaultValue = "1500",
                        description = "The rate limit of the rest calls to the HiveMQ API endpoint in requests per second (default 1500 rps)",
                        order = 3)
    private double rateLimit;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"--format"},
                        defaultValue = "csv",
                        description = "The export output format (default csv)",
                        order = 4)
    private @NotNull OutputFormat format;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--csvSeparator"},
                        defaultValue = "" + CSVWriter.DEFAULT_SEPARATOR,
                        description = "The separator for CSV export (default " + CSVWriter.DEFAULT_SEPARATOR + ")",
                        order = 5)
    private char csvSeparator;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--csvQuoteChar"},
                        defaultValue = "" + CSVWriter.DEFAULT_QUOTE_CHARACTER,
                        description = "The quote character for csv export (default " +
                                CSVWriter.DEFAULT_QUOTE_CHARACTER +
                                ")",
                        order = 6)
    private char csvQuoteCharacter;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--csvEscChar"},
                        defaultValue = "" + CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        description = "The escape character for csv export (default " +
                                CSVWriter.DEFAULT_ESCAPE_CHARACTER +
                                ")",
                        order = 7)
    private char csvEscapeChar;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"--csvLineEndChar"},
                        defaultValue = CSVWriter.DEFAULT_LINE_END,
                        description = "The line-end character for csv export (default \\n)",
                        order = 8)
    private @NotNull String csvLineEndCharacter;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)",
                        order = 9)
    private void initLogging(final boolean logToLogfile) {
        LoggerUtils.turnOffConsoleLogging(logToLogfile);
    }

    private final static @NotNull String DEFAULT_FILE_NAME = "hivemq_client_details";
    private final static int CLIENT_IDS_QUEUE_LIMIT = 100_000;
    private final static int CLIENT_DETAILS_QUEUE_LIMIT = 10_000;

    @Inject
    public ExportClientsCommand() {
    }

    @Override
    public @NotNull Integer call() throws IOException, InterruptedException, ExecutionException {
        Logger.trace("Command {}", this);

        // For now only CSV is supported as output format
        assert format == OutputFormat.csv;

        // Check if given URL is valid
        final HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            Logger.error("URL is not in a valid format: {}", url);
            System.err.println("URL is not in a valid format: " + url);
            return -1;
        }

        // If no file is given create a new file with a current timestamp
        final String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        if (file == null) {
            final String fileType = OutputFormat.csv.name();
            file = new File(DEFAULT_FILE_NAME + "_" + timestamp + "." + fileType);
        }

        // Setup rest service and queues
        final MqttClientsApi mqttClientsApi = new HiveMQRestService().getMqttClientsApi(url, rateLimit);
        final BlockingQueue<String> clientIdsQueue = new LinkedBlockingQueue<>(CLIENT_IDS_QUEUE_LIMIT);
        final BlockingQueue<ClientDetails> clientDetailsQueue = new LinkedBlockingQueue<>(CLIENT_DETAILS_QUEUE_LIMIT);

        Logger.info("Starting export of client details for HiveMQ at {}", url);

        // Start retrieving client ids
        final ClientIdsRetrieverTask clientIdsRetrieverTask =
                new ClientIdsRetrieverTask(mqttClientsApi, clientIdsQueue);
        final CompletableFuture<Void> clientIdsRetrieverFuture = CompletableFuture.runAsync(clientIdsRetrieverTask);

        // Start retrieving client details
        final ClientDetailsRetrieverTask clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(mqttClientsApi,
                clientIdsRetrieverFuture,
                clientIdsQueue,
                clientDetailsQueue);
        final CompletableFuture<Void> clientDetailsRetrieverFuture =
                CompletableFuture.runAsync(clientDetailsRetrieverTask);

        //Fix line end character if "\n" or "\r" are passed
        switch (csvLineEndCharacter) {
            case "\\n":
                csvLineEndCharacter = "\n";
                break;
            case "\\r":
                csvLineEndCharacter = "\r";
                break;
        }

        // Start writing client details
        final ClientDetailsCsvWriterTask clientDetailsCsvWriterTask = new ClientDetailsCsvWriterTask(
                clientDetailsRetrieverFuture,
                clientDetailsQueue,
                file,
                csvSeparator,
                csvQuoteCharacter,
                csvEscapeChar,
                csvLineEndCharacter);
        final CompletableFuture<Void> clientDetailsCsvWriterFuture =
                CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        // Start printing
        final ScheduledExecutorService printingScheduler = Executors.newScheduledThreadPool(1);
        printingScheduler.scheduleWithFixedDelay(new PrintingTask(clientIdsRetrieverTask,
                clientIdsRetrieverFuture,
                clientDetailsCsvWriterTask), 100, 500, TimeUnit.MILLISECONDS);


        // Handle completion of all futures
        final CompletableFuture<Void> exportFuture = CompletableFuture.allOf(clientIdsRetrieverFuture,
                clientDetailsRetrieverFuture,
                clientDetailsCsvWriterFuture);
        final CompletableFuture<Integer> exportResultFuture =
                exportFuture.handle(new ExportCompletedHandler(clientDetailsCsvWriterTask, printingScheduler));

        // Join all future
        final Integer exitCode = exportResultFuture.get();

        Logger.info("Finished export of client details");

        return exitCode;
    }

    @Override
    public @NotNull String toString() {
        return "ExportClientsCommand{" +
                "url='" +
                url +
                '\'' +
                ", file=" +
                file +
                ", rateLimit=" +
                rateLimit +
                ", format=" +
                format +
                ", csvSeparator=" +
                csvSeparator +
                ", csvQuoteCharacter=" +
                csvQuoteCharacter +
                ", csvEscapeChar=" +
                csvEscapeChar +
                ", csvLineEndCharacter='" +
                csvLineEndCharacter +
                '\'' +
                '}';
    }

    private static class PrintingTask implements Runnable {

        private final @NotNull ClientIdsRetrieverTask clientIdsRetrieverTask;
        private final @NotNull CompletableFuture<Void> clientIdsRetrieverFuture;
        private final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask;
        private long lastReported = -1;

        public PrintingTask(
                final @NotNull ClientIdsRetrieverTask clientIdsRetrieverTask,
                final @NotNull CompletableFuture<Void> clientIdsRetrieverFuture,
                final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask) {
            this.clientIdsRetrieverTask = clientIdsRetrieverTask;
            this.clientIdsRetrieverFuture = clientIdsRetrieverFuture;
            this.clientDetailsCsvWriterTask = clientDetailsCsvWriterTask;
        }

        public void run() {
            final long newValue = clientDetailsCsvWriterTask.getWrittenClientDetails();
            if (newValue != lastReported) {
                lastReported = newValue;
                if (clientIdsRetrieverFuture.isDone()) {
                    final long receivedClientIds = clientIdsRetrieverTask.getReceivedClientIds();
                    System.out.append("\rExporting client details: ")
                            .append(String.valueOf(lastReported))
                            .append(" / ")
                            .append(String.valueOf(receivedClientIds))
                            .flush();
                } else {
                    System.out.append("\rExporting client details: ").append(String.valueOf(lastReported)).flush();
                }
            }
        }
    }

    private class ExportCompletedHandler implements BiFunction<Void, Throwable, Integer> {

        private final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask;
        private final @NotNull ScheduledExecutorService printingScheduler;

        public ExportCompletedHandler(
                final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask,
                final @NotNull ScheduledExecutorService printingScheduler) {
            this.clientDetailsCsvWriterTask = clientDetailsCsvWriterTask;
            this.printingScheduler = printingScheduler;
        }

        @Override
        public @NotNull Integer apply(final @Nullable Void o, final @Nullable Throwable throwable) {
            printingScheduler.shutdown();
            if (throwable != null) {
                if (throwable.getCause() instanceof ApiException) {
                    final ApiException apiException = (ApiException) throwable.getCause();
                    if (apiException.getResponseBody() != null) {
                        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        try {
                            final JsonElement je = JsonParser.parseString(apiException.getResponseBody());
                            final String jsonString = gson.toJson(je);
                            System.err.println("\rFailed to retrieve client details: " +
                                    Throwables.getRootCause(throwable).getMessage());
                            System.err.println(jsonString);
                        } catch (final JsonParseException jsonEx) {
                            System.err.println(
                                    "\rFailed to retrieve client details. Please check the URL for the HiveMQ REST-API");
                        }
                    } else {
                        System.err.println("\rFailed to retrieve client details: " +
                                Throwables.getRootCause(throwable).getMessage());
                    }
                } else {
                    System.err.println("\rFailed to retrieve client details: " +
                            Throwables.getRootCause(throwable).getMessage());
                }

                if (clientDetailsCsvWriterTask.getWrittenClientDetails() > 0) {
                    System.out.println("Wrote " +
                            clientDetailsCsvWriterTask.getWrittenClientDetails() +
                            " client details to " +
                            Objects.requireNonNull(file).getPath());
                } else {
                    Objects.requireNonNull(file).delete();
                }

                return -1; // Export failed
            } else {
                System.out.println("\rSuccessfully exported " +
                        clientDetailsCsvWriterTask.getWrittenClientDetails() +
                        " client details to " +
                        Objects.requireNonNull(file).getPath());
                return 0; // Export was successful
            }
        }
    }
}
