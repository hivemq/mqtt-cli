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

import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.Client;
import com.hivemq.cli.openapi.hivemq.ClientList;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientIdsRetrieverTask implements Runnable {

    private @NotNull HiveMQRestService hivemqRestService;
    private final @NotNull BlockingQueue<String> clientIdsQueue;
    private long receivedClientIds = 0;

    private static final Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    public ClientIdsRetrieverTask(final @NotNull HiveMQRestService hivemqRestService,
                                  final @NotNull BlockingQueue<String> clientIdsQueue) {

        this.hivemqRestService = hivemqRestService;
        this.clientIdsQueue = clientIdsQueue;
    }


    public long getReceivedClientIds() { return receivedClientIds; }

    @Override
    public void run() {
        boolean hasNextCursor = true;
        String nextCursor = null;
        while (hasNextCursor) {
            final ClientList clientList;
            try {
                clientList = hivemqRestService.getClientIds(nextCursor);
            } catch (ApiException e) {
                throw new CompletionException(e);
            }
            final List<Client> clients = clientList.getItems();
            final PaginationCursor links = clientList.getLinks();

            if (clients != null) {
                receivedClientIds += clients.size();
                for (final Client client : clients) {
                    if (client.getId() != null) {
                        try {
                            clientIdsQueue.put(client.getId());
                        } catch (InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    }
                }
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

        }
    }
}