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

import com.hivemq.cli.openapi.hivemq.Client;
import com.hivemq.cli.openapi.hivemq.ClientList;
import com.hivemq.cli.openapi.hivemq.MqttClientsApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientIdsRetrieverTask implements Runnable {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull BlockingQueue<String> clientIdsQueue;
    private final @NotNull MqttClientsApi mqttClientsApi;

    private long receivedClientIds = 0;

    public ClientIdsRetrieverTask(
            final @NotNull MqttClientsApi mqttClientsApi, final @NotNull BlockingQueue<String> clientIdsQueue) {
        this.mqttClientsApi = mqttClientsApi;
        this.clientIdsQueue = clientIdsQueue;
    }

    @Override
    public void run() {
        boolean hasNextCursor = true;
        String nextCursor = null;
        try {
            while (hasNextCursor) {
                final ClientList clientList;
                clientList = mqttClientsApi.getAllMqttClients(2500, nextCursor);
                final List<Client> clients = clientList.getItems();
                final PaginationCursor links = clientList.getLinks();

                if (clients != null) {
                    receivedClientIds += clients.size();
                    for (final Client client : clients) {
                        if (client.getId() != null) {
                            clientIdsQueue.put(client.getId());
                        }
                    }
                }

                if (links != null && links.getNext() != null) {
                    final Matcher m = CURSOR_PATTERN.matcher(links.getNext());
                    if (m.find()) {
                        nextCursor = m.group(1);
                    } else {
                        hasNextCursor = false;
                    }
                } else {
                    hasNextCursor = false;
                }
            }
        } catch (final Exception ex) {
            Logger.error(ex, "Retrieval of client ids failed");
            throw new CompletionException(ex);
        }
        Logger.debug("Finished retrieving {} client ids", receivedClientIds);
    }

    public long getReceivedClientIds() {
        return receivedClientIds;
    }
}
