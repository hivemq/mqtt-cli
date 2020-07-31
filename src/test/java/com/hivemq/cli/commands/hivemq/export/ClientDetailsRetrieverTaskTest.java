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
package com.hivemq.cli.commands.hivemq.export;

import com.hivemq.cli.commands.hivemq.export.clients.ClientDetailsRetrieverTask;
import com.hivemq.cli.openapi.hivemq.ClientDetails;
import com.hivemq.cli.rest.HiveMQRestService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    HiveMQRestService hiveMQRestService;
    Future<Void> clientIdsFuture;
    MockWebServer server;
    BlockingQueue<String> clientIdsQueue;
    BlockingQueue<ClientDetails> clientDetailsQueue;
    ClientDetailsRetrieverTask clientDetailsRetrieverTask;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        clientIdsFuture = mock(Future.class);
        when(clientIdsFuture.isDone()).thenReturn(false);

        clientIdsQueue = new LinkedBlockingQueue<>();
        clientDetailsQueue = new LinkedBlockingQueue<>();
        hiveMQRestService = new HiveMQRestService(server.url("/").toString(), 500);

        clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(hiveMQRestService, clientIdsFuture, clientIdsQueue, clientDetailsQueue);
    }


    @Test
    void test_one_detail_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_DETAILS_ALL)
        );


        final Future<Void> completableFuture = Executors.newSingleThreadExecutor().submit(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();


        assertEquals(1, clientDetailsQueue.size());
    }

    @Test
    void test_one_persistent_details_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE)
        );

        final Future<Void> completableFuture = Executors.newSingleThreadExecutor().submit(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();


        assertEquals(1, clientDetailsQueue.size());
    }

    @Test
    void test_one_connected_details_success() throws ExecutionException, InterruptedException {
        clientIdsQueue.add("client-1");
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_DETAILS_CONNECTED)
        );

        final Future<Void> completableFuture = Executors.newSingleThreadExecutor().submit(clientDetailsRetrieverTask);
        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();


        assertEquals(1, clientDetailsQueue.size());
    }



    @Test
    void test_50_details_success() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 25; i++) {
            clientIdsQueue.add("client-" + i);
            server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE)
            );
        }

        final Future<Void> completableFuture = Executors.newSingleThreadExecutor().submit(clientDetailsRetrieverTask);

        for (int i = 25; i < 50; i++) {
            clientIdsQueue.add("client-" + i);
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(CLIENT_DETAILS_PERSISTENT_OFFLINE)
            );
        }

        when(clientIdsFuture.isDone()).thenReturn(true);

        completableFuture.get();


        assertEquals(50, clientDetailsQueue.size());
    }

    @Test
    void test_blocking_client_ids_queue_success() throws InterruptedException {
        clientIdsQueue = new LinkedBlockingQueue<>(1);
        clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(hiveMQRestService, clientIdsFuture, clientIdsQueue, clientDetailsQueue);

        final ExecutorService threadPool = Executors.newFixedThreadPool(2);
        final CompletionService<Void> tasksCompletionService = new ExecutorCompletionService<>(threadPool);

        tasksCompletionService.submit(() -> {
            for (int i = 0; i < 25; i++) {
                Thread.sleep(50);
                clientIdsQueue.put("client-" + i);
                server.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setBody(CLIENT_DETAILS_ALL)
                );
            }
            when(clientIdsFuture.isDone()).thenReturn(true);
            return null;
        });

        tasksCompletionService.submit(clientDetailsRetrieverTask);


        tasksCompletionService.take();
        tasksCompletionService.take();

        assertEquals(25, clientDetailsQueue.size());

    }

    @Test
    void test_blocking_client_details_queue_success() throws InterruptedException {
        clientDetailsQueue = new LinkedBlockingQueue<>(1);
        clientDetailsRetrieverTask = new ClientDetailsRetrieverTask(hiveMQRestService, clientIdsFuture, clientIdsQueue, clientDetailsQueue);

        for (int i = 0; i < 25; i++) {
            Thread.sleep(50);
            clientIdsQueue.put("client-" + i);
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(CLIENT_DETAILS_ALL)
            );
        }
        when(clientIdsFuture.isDone()).thenReturn(true);

        final ExecutorService threadPool = Executors.newFixedThreadPool(2);
        final CompletionService<Void> tasksCompletionService = new ExecutorCompletionService<>(threadPool);


        final Future<Void> clientDetailsRetrieverFuture = tasksCompletionService.submit(clientDetailsRetrieverTask);


        final AtomicInteger receivedClientDetails = new AtomicInteger(0);
        tasksCompletionService.submit(() -> {
            while (!clientDetailsRetrieverFuture.isDone() ||  !clientDetailsQueue.isEmpty()) {
                final ClientDetails clientDetails = clientDetailsQueue.poll(50, TimeUnit.MILLISECONDS);
                if (clientDetails != null) {
                    receivedClientDetails.incrementAndGet();
                }
            }
            return null;
        });



        tasksCompletionService.take();
        tasksCompletionService.take();

        assertEquals(25, receivedClientDetails.get());

    }
}