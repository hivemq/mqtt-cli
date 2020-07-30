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

import com.hivemq.cli.commands.hivemq.export.clients.ClientIdsRetrieverTask;
import com.hivemq.cli.rest.HiveMQRestService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiException;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.hivemq.cli.rest.ClientsApiResponses.*;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_IDS_INVALID_CURSOR;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_IDS_REPLICATION;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_IDS_SINGLE_RESULT;
import static com.hivemq.cli.rest.hivemq.TestResponseBodies.CLIENT_IDS_WITH_CURSOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ClientIdsRetrieverTaskTest {

    HiveMQRestService hiveMQRestService;
    MockWebServer server;
    Queue<String> clientIdsQueue;
    ClientIdsRetrieverTask clientIdsRetrieverTask;


    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        clientIdsQueue = new ConcurrentLinkedQueue<>();
        hiveMQRestService = new HiveMQRestService(server.url("/").toString(), 500);

        clientIdsRetrieverTask = new ClientIdsRetrieverTask(hiveMQRestService, clientIdsQueue);
    }

    @Test
    void test_single_page_success() throws ApiException, InterruptedException {
        final MockResponse response = new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_IDS_SINGLE_RESULT);

        server.enqueue(response);

        clientIdsRetrieverTask.call();

        assertEquals(1, clientIdsQueue.size());
        assertEquals("client-ݰ", clientIdsQueue.poll());
    }

    @Test
    void test_more_pages_success() throws ApiException, InterruptedException {
        final MockResponse response = new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_IDS_WITH_CURSOR);

        server.enqueue(response);
        server.enqueue(response);
        server.enqueue(response);
        server.enqueue(response);
        server.enqueue(response);

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_IDS_SINGLE_RESULT));

        clientIdsRetrieverTask.call();

        assertEquals(51, clientIdsQueue.size());
    }

    @Test
    void test_retry_success() throws ApiException, InterruptedException {
        clientIdsRetrieverTask.setRetryIntervalInSeconds(2);

        final long startTime = System.nanoTime();

        final MockResponse response = new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_IDS_WITH_CURSOR);

        server.enqueue(response);

        server.enqueue(new MockResponse()
                .setResponseCode(HIVEMQ_IN_REPLICATION)
                .setBody(CLIENT_IDS_REPLICATION));

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(CLIENT_IDS_SINGLE_RESULT));

        clientIdsRetrieverTask.call();


        final long stopTime = System.nanoTime();
        assertEquals(11, clientIdsQueue.size());

        assertTrue(stopTime >= startTime + 2_000_000);
    }

    @Test
    void test_unrecoverable_exception_success() {
        final MockResponse response = new MockResponse()
                .setResponseCode(INVALID_CURSOR_VALUE)
                .setBody(CLIENT_IDS_INVALID_CURSOR);

        server.enqueue(response);

        assertThrows(ApiException.class, () -> clientIdsRetrieverTask.call());
        assertEquals(0, clientIdsQueue.size());
    }
}