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

import com.hivemq.cli.openapi.ApiCallback;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.openapi.hivemq.ClientItem;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientDetailsRetrieverTask implements Runnable {

    private final static int MAX_CONCURRENT_REQUESTS = 100;

    private final @NotNull MqttClientsApi mqttClientsApi;
    private final @NotNull CompletableFuture<Void> clientIdsFuture;
    private final @NotNull BlockingQueue<String> clientIdsQueue;
    private final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue;
    private final @NotNull Semaphore clientDetailsInProgress;
    private final @NotNull AtomicBoolean failed = new AtomicBoolean(false);

    public ClientDetailsRetrieverTask(
            final @NotNull MqttClientsApi mqttClientsApi,
            final @NotNull CompletableFuture<Void> clientIdsFuture,
            final @NotNull BlockingQueue<String> clientIdsQueue,
            final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue) {
        this.mqttClientsApi = mqttClientsApi;
        this.clientIdsFuture = clientIdsFuture;
        this.clientIdsQueue = clientIdsQueue;
        this.clientDetailsQueue = clientDetailsQueue;
        clientDetailsInProgress = new Semaphore(MAX_CONCURRENT_REQUESTS);
    }

    @Override
    public void run() {
        try {
            while (!clientIdsFuture.isDone() || !clientIdsQueue.isEmpty()) {
                if (failed.get()) {
                    Logger.error("Retrieval of client details failed");
                    throw new CompletionException(new RuntimeException("Retrieval of client details failed"));
                }

                final String clientId = clientIdsQueue.poll(50, TimeUnit.MILLISECONDS);
                if (clientId != null) {
                    final ClientItemApiCallback clientItemApiCallback =
                            new ClientItemApiCallback(clientDetailsQueue, clientDetailsInProgress, failed);
                    clientDetailsInProgress.acquire();
                    mqttClientsApi.getMqttClientDetailsAsync(clientId, clientItemApiCallback);
                }
            }

            // Block until all callbacks are finished
            clientDetailsInProgress.acquire(MAX_CONCURRENT_REQUESTS);
        } catch (final Exception e) {
            Logger.error(e, "Retrieval of client details failed");
            throw new CompletionException(e);
        }
        Logger.debug("Finished retrieving client details");
    }

    private static class ClientItemApiCallback implements ApiCallback<ClientItem> {

        private final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue;
        private final @NotNull Semaphore clientDetailsInProgress;
        private final @NotNull AtomicBoolean failed;

        public ClientItemApiCallback(
                final @NotNull BlockingQueue<ClientDetails> clientDetailsQueue,
                final @NotNull Semaphore clientDetailsInProgress,
                final @NotNull AtomicBoolean failed) {
            this.clientDetailsQueue = clientDetailsQueue;
            this.clientDetailsInProgress = clientDetailsInProgress;
            this.failed = failed;
        }

        @Override
        public void onSuccess(
                final @NotNull ClientItem result,
                final int statusCode,
                final @NotNull Map<String, List<String>> responseHeaders) {
            final ClientDetails clientDetails = result.getClient();
            if (clientDetails != null) {
                try {
                    clientDetailsQueue.put(clientDetails);
                } catch (final InterruptedException ignored) {
                }
            }
            clientDetailsInProgress.release();
        }

        @Override
        public void onFailure(
                final @NotNull ApiException e,
                final int statusCode,
                final @NotNull Map<String, List<String>> responseHeaders) {
            //ignore 404 because MQTT client could be non-persistent and disconnected by now
            if (e.getCode() != 404) {
                Logger.trace(e, "Failed to retrieve client details");
                failed.set(true);
            }
            clientDetailsInProgress.release();
        }

        @Override
        public void onUploadProgress(final long bytesWritten, final long contentLength, final boolean done) {
        }

        @Override
        public void onDownloadProgress(final long bytesRead, final long contentLength, final boolean done) {
        }
    }
}
