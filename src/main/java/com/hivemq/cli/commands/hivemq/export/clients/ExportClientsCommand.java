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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.export.AbstractExportCommand;
import com.hivemq.cli.rest.HiveMQRestService;
import org.openapitools.client.ApiException;
import org.openapitools.client.model.ClientDetails;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@CommandLine.Command(
        name = "clients",
        description = "Export HiveMQ client details",
        sortOptions = false,
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class ExportClientsCommand extends AbstractExportCommand implements Callable<Integer> {

    private final static String DEFAULT_FILE_NAME = "hivemq_client_details";
    final static int CLIENT_IDS_QUEUE_LIMIT = 10_000;
    final static int CLIENT_DETAILS_QUEUE_LIMIT = 10_000;

    @Inject
    public ExportClientsCommand() { }

    @Override
    public Integer call() throws IOException {

        // For now only CSV is supported as output format
        assert format == OutputFormat.csv;

        final String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        if (file == null) {
            final String fileType = OutputFormat.csv.name();
            file = new File(DEFAULT_FILE_NAME + "_" + timestamp + "." + fileType);
        }

        final HiveMQRestService hivemqRestService = new HiveMQRestService(url, rateLimit);
        final ExecutorService threadPool = Executors.newFixedThreadPool(3);
        final CompletionService<Void> tasksCompletionService = new ExecutorCompletionService<>(threadPool);
        final BlockingQueue<String> clientIdsQueue = new LinkedBlockingQueue<>(CLIENT_IDS_QUEUE_LIMIT);
        final BlockingQueue<ClientDetails> clientDetailsQueue = new LinkedBlockingQueue<>(CLIENT_DETAILS_QUEUE_LIMIT);

        // Start retrieving client ids
        final ClientIdsRetrieverTask clientIdsRetrieverTask = new ClientIdsRetrieverTask(hivemqRestService, clientIdsQueue);
        final Future<Void> clientIdsRetrieverFuture = tasksCompletionService.submit(clientIdsRetrieverTask);


        // Start retrieving client details
        final ClientDetailsRetrieverTask clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(
                hivemqRestService,
                clientIdsRetrieverFuture,
                clientIdsQueue,
                clientDetailsQueue
        );
        final Future<Void> clientDetailsRetrieverFuture = tasksCompletionService.submit(clientDetailsRetrieverTask);


        // Start writing client details
        final ClientDetailsCsvWriterTask clientDetailsCsvWriterTask = new ClientDetailsCsvWriterTask(
                clientDetailsRetrieverFuture,
                clientDetailsQueue,
                file,
                csvLineSeparator,
                csvQuoteCharacter,
                csvEscapeChar,
                csvLineEndCharacter);

        final Future<?> clientDetailsWriterFuture = tasksCompletionService.submit(clientDetailsCsvWriterTask);

        ScheduledExecutorService printingScheduler = Executors.newScheduledThreadPool(1);

        ScheduledFuture<?> f = printingScheduler.scheduleWithFixedDelay(new Runnable() {
            long lastReported = -1;
            public void run() {
                long newValue = clientDetailsCsvWriterTask.getWrittenClientDetails();
                if(newValue != lastReported) {
                    lastReported = newValue;
                    if (clientIdsRetrieverFuture.isDone()) {
                        final long receivedClientIds = clientIdsRetrieverTask.getReceivedClientIds();
                        System.out.append("\rExporting client details: " + lastReported + " / " + receivedClientIds).flush();
                    }
                    else {
                        System.out.append("\rExporting client details: " + lastReported).flush();
                    }
                }
            }
        }, 100, 500, TimeUnit.MILLISECONDS);


        for (int i = 0; i < 3; i++) {
            try {
                final Future<?> result = tasksCompletionService.take();
                result.get();
            }
            catch (InterruptedException | ExecutionException e) {
                threadPool.shutdown();
                printingScheduler.shutdown();
                System.err.println("\rFailed to retrieve client details: " + Throwables.getRootCause(e).getMessage());
                if (e.getCause() instanceof ApiException) {
                    final ApiException apiException = (ApiException) e.getCause();
                    if (apiException.getResponseBody() != null) {
                        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        final JsonElement je = JsonParser.parseString(apiException.getResponseBody());
                        final String jsonString = gson.toJson(je);
                        System.err.println(jsonString);
                    }
                }
                if (clientDetailsCsvWriterTask.getWrittenClientDetails() > 0) {
                    System.out.println("Wrote " + clientDetailsCsvWriterTask.getWrittenClientDetails() + " client details to " + file.getPath());
                }
                else {
                    file.delete();
                }
                return -1;
            }
        }
        printingScheduler.shutdown();

        System.out.println("\rSuccessfully exported " + clientDetailsCsvWriterTask.getWrittenClientDetails() + " client details to " + file.getPath());

        return 0;
    }





}

