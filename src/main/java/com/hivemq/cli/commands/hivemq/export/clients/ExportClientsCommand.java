/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.commands.hivemq.export.clients;


import com.google.common.base.Throwables;
import com.google.gson.*;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.export.AbstractExportCommand;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.rest.HiveMQRestService;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.BiFunction;


@CommandLine.Command(
        name = "clients",
        description = "Export HiveMQ client details",
        sortOptions = false,
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class ExportClientsCommand extends AbstractExportCommand implements Callable<Integer> {

    final static int CLIENT_IDS_QUEUE_LIMIT = 100_000;
    final static int CLIENT_DETAILS_QUEUE_LIMIT = 10_000;
    private final static String DEFAULT_FILE_NAME = "hivemq_client_details";

    @Inject
    public ExportClientsCommand() {
    }

    @Override
    public Integer call() throws IOException, InterruptedException, ExecutionException {
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
        final HiveMQRestService hivemqRestService = new HiveMQRestService(url, rateLimit);
        final BlockingQueue<String> clientIdsQueue = new LinkedBlockingQueue<>(CLIENT_IDS_QUEUE_LIMIT);
        final BlockingQueue<ClientDetails> clientDetailsQueue = new LinkedBlockingQueue<>(CLIENT_DETAILS_QUEUE_LIMIT);

        Logger.info("Starting export of client details for HiveMQ at {} ", url);

        // Start retrieving client ids
        final ClientIdsRetrieverTask clientIdsRetrieverTask = new ClientIdsRetrieverTask(hivemqRestService, clientIdsQueue);
        final CompletableFuture<Void> clientIdsRetrieverFuture = CompletableFuture.runAsync(clientIdsRetrieverTask);

        // Start retrieving client details
        final ClientDetailsRetrieverTask clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(
                hivemqRestService,
                clientIdsRetrieverFuture,
                clientIdsQueue,
                clientDetailsQueue
        );
        final CompletableFuture<Void> clientDetailsRetrieverFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);

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
        final CompletableFuture<Void> clientDetailsCsvWriterFuture = CompletableFuture.runAsync(clientDetailsCsvWriterTask);

        // Start printing
        final ScheduledExecutorService printingScheduler = Executors.newScheduledThreadPool(1);
        printingScheduler.scheduleWithFixedDelay(
                new PrintingTask(clientIdsRetrieverTask, clientIdsRetrieverFuture, clientDetailsCsvWriterTask),
                100, 500, TimeUnit.MILLISECONDS);


        // Handle completion of all futures
        final CompletableFuture<Void> exportFuture = CompletableFuture.allOf(clientIdsRetrieverFuture, clientDetailsRetrieverFuture, clientDetailsCsvWriterFuture);
        final CompletableFuture<Integer> exportResultFuture = exportFuture.handle(new ExportCompletedHandler(clientDetailsCsvWriterTask, printingScheduler));

        // Join all future
        final Integer exitCode = exportResultFuture.get();

        Logger.info("Finished export of client details");

        return exitCode;
    }


    private static class PrintingTask implements Runnable {
        private final @NotNull ClientIdsRetrieverTask clientIdsRetrieverTask;
        private final @NotNull CompletableFuture<Void> clientIdsRetrieverFuture;
        private final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask;
        private long lastReported = -1;


        public PrintingTask(final @NotNull ClientIdsRetrieverTask clientIdsRetrieverTask,
                            final @NotNull CompletableFuture<Void> clientIdsRetrieverFuture,
                            final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask) {
            this.clientIdsRetrieverTask = clientIdsRetrieverTask;
            this.clientIdsRetrieverFuture = clientIdsRetrieverFuture;
            this.clientDetailsCsvWriterTask = clientDetailsCsvWriterTask;
        }

        public void run() {
            long newValue = clientDetailsCsvWriterTask.getWrittenClientDetails();
            if (newValue != lastReported) {
                lastReported = newValue;
                if (clientIdsRetrieverFuture.isDone()) {
                    final long receivedClientIds = clientIdsRetrieverTask.getReceivedClientIds();
                    System.out.append("\rExporting client details: " + lastReported + " / " + receivedClientIds).flush();
                } else {
                    System.out.append("\rExporting client details: " + lastReported).flush();
                }
            }
        }

    }

    private class ExportCompletedHandler implements BiFunction<Void, Throwable, Integer> {
        private final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask;
        private final @NotNull ScheduledExecutorService printingScheduler;

        public ExportCompletedHandler(final @NotNull ClientDetailsCsvWriterTask clientDetailsCsvWriterTask,
                                      final @NotNull ScheduledExecutorService printingScheduler) {
            this.clientDetailsCsvWriterTask = clientDetailsCsvWriterTask;
            this.printingScheduler = printingScheduler;
        }


        @Override
        public @NotNull Integer apply(Void o, Throwable throwable) {
            printingScheduler.shutdown();
            if (throwable != null) {
                if (throwable.getCause() instanceof ApiException) {
                    final ApiException apiException = (ApiException) throwable.getCause();
                    if (apiException.getResponseBody() != null) {
                        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        try {
                            final JsonElement je = JsonParser.parseString(apiException.getResponseBody());
                            final String jsonString = gson.toJson(je);
                            System.err.println("\rFailed to retrieve client details: " + Throwables.getRootCause(throwable).getMessage());
                            System.err.println(jsonString);
                        } catch (final JsonParseException jsonEx) {
                            System.err.println("\rFailed to retrieve client details. Please check the URL for the HiveMQ REST-API");
                        }
                    } else {
                        System.err.println("\rFailed to retrieve client details: " + Throwables.getRootCause(throwable).getMessage());
                    }
                } else {
                    System.err.println("\rFailed to retrieve client details: " + Throwables.getRootCause(throwable).getMessage());
                }

                if (clientDetailsCsvWriterTask.getWrittenClientDetails() > 0) {
                    System.out.println("Wrote " + clientDetailsCsvWriterTask.getWrittenClientDetails() + " client details to " + file.getPath());
                } else {
                    file.delete();
                }

                return -1; // Export failed
            } else {
                System.out.println("\rSuccessfully exported " + clientDetailsCsvWriterTask.getWrittenClientDetails() + " client details to " + file.getPath());
                return 0; // Export was successful
            }
        }
    }
}
