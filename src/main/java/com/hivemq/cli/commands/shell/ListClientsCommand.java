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
package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.mqtt.ClientData;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CommandLine.Command(name = "ls",
        aliases = "list",
        description = "List all connected clients with their respective identifieres"
)

public class ListClientsCommand implements Runnable, CliCommand {

    private final MqttClientExecutor mqttClientExecutor;

    public ListClientsCommand() {
        this(null);
    }

    @Inject
    ListClientsCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t"}, defaultValue = "false", description = "sort by creation time, newest first")
    private boolean sortByTime;

    @CommandLine.Option(names = {"-U"}, defaultValue = "false", description = "do not sort")
    private boolean doNotSort;

    @CommandLine.Option(names = {"-r", "--reverse"}, defaultValue = "false", description = "reverse order while sorting")
    private boolean reverse;

    @CommandLine.Option(names = {"-l", "--long"}, defaultValue = "false", description = "use a long listing format")
    private boolean longOutput;

    @CommandLine.Option(names = {"-s", "--subscriptions"}, defaultValue = "false", description = "list subscribed topics of clients")
    private boolean listSubscriptions;

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command {}", this);
        }

        final List<ClientData> sortedClientData = getSortedClientData();

        if (longOutput) {
            System.out.println("total " + sortedClientData.size());

            if (sortedClientData.size() == 0) {
                return;
            }


            final Set<MqttClient> clients = sortedClientData.stream()
                    .map(clientData -> clientData.getClient())
                    .collect(Collectors.toSet());

            final int longestID = clients.stream()
                    .map(c -> c.getConfig().getClientIdentifier().get().toString().length())
                    .max(Integer::compareTo)
                    .get();

            final int longestHost = clients.stream()
                    .map(c -> c.getConfig().getServerHost().length())
                    .max(Integer::compareTo)
                    .get();

            final int longestState = clients.stream()
                    .map(c -> c.getConfig().getState().toString().length())
                    .max(Integer::compareTo)
                    .get();

            final int longestVersion = clients.stream()
                    .map(c -> c.getConfig().getMqttVersion().toString().length())
                    .max(Integer::compareTo)
                    .get();

            final int longestSSLVersion = clients.stream()
                    .map(c -> c.getConfig().getSslConfig().toString().length())
                    .max(Integer::compareTo)
                    .orElse("NO_SSL".length());

            final String format = new String("%-" + longestState + "s " +
                    "%02d:%02d:%02d " +
                    "%-" + longestID + "s " +
                    "%-" + longestHost + "s " +
                    "%5d " +
                    "%-" + longestVersion + "s " +
                    "%-" + longestSSLVersion + "s\n");

            for (final ClientData clientData : sortedClientData) {

                final MqttClient client = clientData.getClient();

                final LocalDateTime dateTime = clientData.getCreationTime();

                final String connectionState = client.getState().toString();

                System.out.printf(format,
                        connectionState,
                        dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(),
                        client.getConfig().getClientIdentifier().get().toString(),
                        client.getConfig().getServerHost(),
                        client.getConfig().getServerPort(),
                        client.getConfig().getMqttVersion().name(),
                        client.getConfig().getSslConfig().map(ssl -> ssl.getProtocols().get().toString()).orElse("NO_SSL"));

                if (listSubscriptions) {
                    System.out.printf(" -subscribed topics: %s\n", clientData.getSubscribedTopics());
                }
            }


        } else {

            for (final ClientData clientData : sortedClientData) {
                System.out.println(clientData.getClient().getConfig().getClientIdentifier().get() + "@" + clientData.getClient().getConfig().getServerHost());
                if (listSubscriptions) {
                    System.out.printf(" -subscribed topics: %s\n", clientData.getSubscribedTopics());
                }
            }
        }


    }


    public List<ClientData> getSortedClientData() {
        List<ClientData> sortedClientData =  new ArrayList<>(MqttClientExecutor.getClientDataMap().values());

        if (doNotSort) {
            return sortedClientData;
        }

        Comparator<ClientData> comparator;
        if (sortByTime) {
            comparator = Comparator.comparing(ClientData::getCreationTime);
        }
        else {
            comparator = Comparator.comparing(clientData -> clientData
                    .getClient()
                    .getConfig()
                    .getClientIdentifier()
                    .map(Object::toString)
                    .orElse(""));
        }
        if (reverse) {
            comparator = comparator.reversed();
        }

        sortedClientData.sort(comparator);
        return sortedClientData;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "sortByTime=" + sortByTime +
                ", doNotSort=" + doNotSort +
                ", reverse=" + reverse +
                ", listSubscriptions" + listSubscriptions +
                ", longOutput=" + longOutput +
                '}';
    }

    private String getKey(final MqttClient client) {
        return client.getConfig().getClientIdentifier() + client.getConfig().getServerHost();
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }
}
