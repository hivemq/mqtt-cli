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

import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import com.hivemq.cli.rest.HiveMQRestService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_DETAILS_ALL;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_DETAILS_CONNECTED;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_DETAILS_PERSISTENT_OFFLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientDetailsRetrieverTaskTest {

    private @NotNull MockWebServer server;
    private @NotNull CompletableFuture<Void> clientIdsFuture;
    private @NotNull BlockingQueue<String> clientIdsQueue;
    private @NotNull BlockingQueue<ClientDetails> clientDetailsQueue;
    private @NotNull MqttClientsApi mqttClientsApi;
    private @NotNull ClientDetailsRetrieverTask clientDetailsRetrieverTask;

    @BeforeEach
    void setUp() throws IOException {
        //noinspection unchecked
        clientIdsFuture = mock(CompletableFuture.class);
        when(clientIdsFuture.isDone()).thenReturn(false);

        server = new MockWebServer();
        server.start();

        clientIdsQueue = new LinkedBlockingQueue<>();
        clientDetailsQueue = new LinkedBlockingQueue<>();
        mqttClientsApi = new HiveMQRestService().getMqttClientsApi(server.url("/").toString(), 500);
        clientDetailsRetrieverTask =
                new ClientDetailsRetrieverTask(mqttClientsApi, clientIdsFuture, clientIdsQueue, clientDetailsQueue);
    }

    @Test
    void one_detail_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_ALL));

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertEquals(1, clientDetailsQueue.size());
    }

    @Test
    void one_persistent_details_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE));

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertEquals(1, clientDetailsQueue.size());
    }

    @Test
    void one_connected_details_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_CONNECTED));

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertEquals(1, clientDetailsQueue.size());
    }

    @Test
    void details_50_success() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 25; i++) {
            clientIdsQueue.add("client-" + i);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE));
        }

        final CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);

        for (int i = 25; i < 50; i++) {
            clientIdsQueue.add("client-" + i);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE));
        }

        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();

        assertEquals(50, clientDetailsQueue.size());
    }

    @Test
    void blocking_client_ids_queue_success() {
        clientIdsQueue = new LinkedBlockingQueue<>(1);
        clientDetailsRetrieverTask =
                new ClientDetailsRetrieverTask(mqttClientsApi, clientIdsFuture, clientIdsQueue, clientDetailsQueue);

        final CompletableFuture<Void> clientIdsProducerFuture = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < 25; i++) {
                    Thread.sleep(50);
                    clientIdsQueue.put("client-" + i);
                    server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_ALL));
                }
            } catch (final Exception ex) {
                throw new CompletionException(ex);
            }
            when(clientIdsFuture.isDone()).thenReturn(true);
        });
        final CompletableFuture<Void> clientDetailsFuture = CompletableFuture.runAsync(clientDetailsRetrieverTask);

        clientIdsProducerFuture.join();
        clientDetailsFuture.join();

        assertEquals(25, clientDetailsQueue.size());
    }

    @Test
    void blocking_client_details_queue_success() throws InterruptedException {
        clientDetailsQueue = new LinkedBlockingQueue<>(1);
        clientDetailsRetrieverTask =
                new ClientDetailsRetrieverTask(mqttClientsApi, clientIdsFuture, clientIdsQueue, clientDetailsQueue);

        for (int i = 0; i < 25; i++) {
            Thread.sleep(50);
            clientIdsQueue.put("client-" + i);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(CLIENT_DETAILS_ALL));
        }
        when(clientIdsFuture.isDone()).thenReturn(true);

        final CompletableFuture<Void> clientDetailsRetrieverFuture =
                CompletableFuture.runAsync(clientDetailsRetrieverTask);

        final AtomicInteger receivedClientDetails = new AtomicInteger(0);
        final CompletableFuture<Void> receivedClientDetailsFuture = CompletableFuture.runAsync(() -> {
            try {

                while (!clientDetailsRetrieverFuture.isDone() || !clientDetailsQueue.isEmpty()) {
                    final ClientDetails clientDetails = clientDetailsQueue.poll(50, TimeUnit.MILLISECONDS);
                    if (clientDetails != null) {
                        receivedClientDetails.incrementAndGet();
                    }
                }
            } catch (final Exception ex) {
                throw new CompletionException(ex);
            }
        });

        clientDetailsRetrieverFuture.join();
        receivedClientDetailsFuture.join();

        assertEquals(25, receivedClientDetails.get());
    }
}
