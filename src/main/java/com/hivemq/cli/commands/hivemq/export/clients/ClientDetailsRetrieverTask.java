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

import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.ApiCallback;
import org.openapitools.client.ApiException;
import org.openapitools.client.model.ClientDetails;
import org.openapitools.client.model.ClientItem;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class ClientDetailsRetrieverTask implements Callable<Void> {

    final @NotNull HiveMQRestService hivemqRestService;
    final @NotNull Future<Void> clientIdsFuture;
    final @NotNull Queue<String> clientIdsQueue;
    final @NotNull Queue<ClientDetails> clientDetailsQueue;
    final @NotNull AtomicLong clientDetailsInProgress;

    final static long IN_PROGRESS_LIMIT = 10_000;
    final static long MAX_CONCURRENT_REQUESTS = 100;

    final @NotNull AtomicLong processedClientDetails = new AtomicLong(0);

    public ClientDetailsRetrieverTask(final @NotNull HiveMQRestService hivemqRestService,
                                      final @NotNull Future<Void> clientIdsFuture,
                                      final @NotNull Queue<String> clientIdsQueue,
                                      final @NotNull Queue<ClientDetails> clientDetailsQueue) {
        this.hivemqRestService = hivemqRestService;
        this.clientIdsFuture = clientIdsFuture;
        this.clientIdsQueue = clientIdsQueue;
        this.clientDetailsQueue = clientDetailsQueue;
        clientDetailsInProgress = new AtomicLong(0);
    }

    @Override
    public Void call() throws InterruptedException, ApiException {

        while (!clientIdsFuture.isDone() || !clientIdsQueue.isEmpty()) {

            while (!clientIdsQueue.isEmpty()) {
                final String clientId = clientIdsQueue.poll();
                final ClientItemApiCallback clientItemApiCallback = new ClientItemApiCallback(clientDetailsQueue, clientDetailsInProgress, processedClientDetails);

                clientDetailsInProgress.incrementAndGet();
                hivemqRestService.getClientDetails(clientId, clientItemApiCallback);

                while (clientDetailsInProgress.get() > MAX_CONCURRENT_REQUESTS && clientDetailsQueue.size() > IN_PROGRESS_LIMIT) {
                    Thread.sleep(50);
                }
            }
        }

        while (clientDetailsInProgress.get() != 0) {
            Thread.sleep(50);
        }
        return null;
    }


    private static class ClientItemApiCallback implements ApiCallback<ClientItem> {
        final @NotNull Queue<ClientDetails> clientDetailsQueue;
        final @NotNull AtomicLong clientDetailsInProgress;
        final @NotNull AtomicLong processedClientDetails;

        public ClientItemApiCallback(final @NotNull Queue<ClientDetails> clientDetailsQueue,
                                     final @NotNull AtomicLong clientDetailsInProgress,
                                     final @NotNull AtomicLong processedClientDetails) {
            this.clientDetailsQueue = clientDetailsQueue;
            this.clientDetailsInProgress = clientDetailsInProgress;
            this.processedClientDetails = processedClientDetails;
        }

        @Override
        public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
            clientDetailsInProgress.decrementAndGet();
        }

        @Override
        public void onSuccess(ClientItem result, int statusCode, Map<String, List<String>> responseHeaders) {
            clientDetailsQueue.add(result.getClient());
            clientDetailsInProgress.decrementAndGet();
            processedClientDetails.incrementAndGet();
        }

        @Override
        public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
        }

        @Override
        public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
        }
    }
}