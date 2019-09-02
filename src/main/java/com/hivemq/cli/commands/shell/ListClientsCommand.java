/*
 * Copyright 2019 dc-square and the HiveMQ Commandline Interface Project
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
package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.cli.AbstractCommand;
import com.hivemq.cli.mqtt.ClientCache;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientConfig;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@CommandLine.Command(name = "ls",
        aliases = "list",
        description = "List all connected clients with their respective identifieres",
        mixinStandardHelpOptions = true)

public class ListClientsCommand extends AbstractCommand implements Runnable {

    private final MqttClientExecutor mqttClientExecutor;

    @Inject
    ListClientsCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @CommandLine.Option(names = {"-t", "--time"}, defaultValue = "false", description = "Sort clients ordered by their creation time (default false)")
    private boolean sortByTime;

    @CommandLine.Option(names = {"-a", "--all"}, defaultValue = "false", description = "List clients with detailed client information (default false)")
    private boolean outputDetailedClientInformation;

    @CommandLine.Option(names = {"-at", "-ta"}, description = "List detailed client information sorted by client creation time")
    private void setOptionsTrue(final boolean allTrue) {
        sortByTime = allTrue;
        outputDetailedClientInformation = allTrue;
    }



    @Override
    public void run() {


        if (isVerbose()) {
            Logger.trace("Command: {}", this);
        }


        final ClientCache<String, MqttClient> clientCache = mqttClientExecutor.getClientCache();
        final Map<String, LocalDateTime> creationTimes = mqttClientExecutor.getClientCreationTimes();
        final Set<String> clientKeys = clientCache.keySet();


        Comparator<String> comparator = (s1, s2) -> {
            final MqttClient client1 = clientCache.get(s1);
            final MqttClient client2 = clientCache.get(s2);
            final String client1Key = getKey(client1);
            final String client2Key = getKey(client2);
            return client1Key.compareTo(client2Key);
        };

        if (sortByTime) {
            comparator = Comparator.comparing(creationTimes::get);
        }

        final TreeMap<String, MqttClient> sortedClients = new TreeMap<>(comparator);

        for (final String key : clientKeys) {
            final MqttClient client = clientCache.get(key);
            if (client.getConfig().getState().isConnectedOrReconnect()) {
                sortedClients.put(key, client);
            }
        }


        String outputFormat = "%-20s %-25s\n";

        if (outputDetailedClientInformation) {
            outputFormat = "%-30s %-20s %-20s %-10s %-25s %-15s %-15s\n";
            System.out.printf(outputFormat,
                    "Created-At",
                    "Client-ID",
                    "Host",
                    "Port",
                    "Server-Address",
                    "MQTT version",
                    "SSL"
            );
        } else {
            System.out.printf(outputFormat,
                    "Client-ID",
                    "Server-Address");
        }

        for (final Map.Entry<String, MqttClient> entry : sortedClients.entrySet()) {

            final String clientKey = entry.getKey();
            final MqttClient client = entry.getValue();
            final MqttClientConfig config = client.getConfig();
            final LocalDateTime timestamp = creationTimes.get(clientKey);

            if (outputDetailedClientInformation) {
                System.out.printf(outputFormat,
                        timestamp,
                        config.getClientIdentifier().orElse(null),
                        config.getServerHost(),
                        config.getServerPort(),
                        config.getServerAddress(),
                        config.getMqttVersion(),
                        config.getSslConfig().map((m) -> "true").orElse("false")
                );
            } else {

                System.out.printf(outputFormat,
                        config.getClientIdentifier().orElse(null),
                        config.getServerAddress());

            }
        }

    }

    @Override
    public String toString() {
        return "List:: {" +
                "sortByTime=" + sortByTime +
                ", detailedOutput=" + outputDetailedClientInformation +
                '}';
    }

    private String getKey(final MqttClient client) {
        return client.getConfig().getClientIdentifier() + client.getConfig().getServerHost();
    }
}
