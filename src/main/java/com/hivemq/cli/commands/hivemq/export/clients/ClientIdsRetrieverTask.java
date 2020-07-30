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
import org.openapitools.client.ApiException;
import org.openapitools.client.model.Client;
import org.openapitools.client.model.ClientList;
import org.openapitools.client.model.PaginationCursor;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hivemq.cli.rest.ClientsApiResponses.HIVEMQ_IN_REPLICATION;

public class ClientIdsRetrieverTask implements Callable<Void> {

    final @NotNull HiveMQRestService hivemqRestService;
    final @NotNull Queue<String> clientIdsQueue;

    final static long QUEUE_LIMIT = 10_000;

    int retryIntervalInSeconds = 10;
    long receivedClientIds = 0;

    private static final Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    public ClientIdsRetrieverTask(final @NotNull HiveMQRestService hivemqRestService,
                                  final @NotNull Queue<String> clientIdsQueue) {

        this.hivemqRestService = hivemqRestService;
        this.clientIdsQueue = clientIdsQueue;
    }


    @Override
    public Void call() throws InterruptedException, ApiException {
        boolean hasNextCursor = true;
        String nextCursor = null;
        while (hasNextCursor) {
            try {
                final ClientList clientList = hivemqRestService.getClientIds(nextCursor);
                final List<Client> clients = clientList.getItems();
                final PaginationCursor links = clientList.getLinks();


                if (clients != null) {
                    receivedClientIds += clients.size();
                    clients.forEach(client -> clientIdsQueue.add(client.getId()));
                }

                while (clientIdsQueue.size() > QUEUE_LIMIT) {
                    Thread.sleep(50);
                }

                if (links != null && links.getNext() != null) {
                    final Matcher m = CURSOR_PATTERN.matcher(links.getNext());
                    if (m.find()) {
                        nextCursor = m.group(1);
                    }
                    else {
                        hasNextCursor = false;
                    }
                } else {
                    hasNextCursor = false;
                }

            } catch (final @NotNull ApiException apiException) {
                final int code = apiException.getCode();

                if (code == HIVEMQ_IN_REPLICATION) {
                    Thread.sleep(retryIntervalInSeconds * 1_000);
                }
                else {
                    throw apiException;
                }
            }
        }
        return null;
    }

    public long getReceivedClientIds() { return receivedClientIds; }

    public void setRetryIntervalInSeconds(final int retryIntervalInSeconds) {
        this.retryIntervalInSeconds = retryIntervalInSeconds;
    }

}