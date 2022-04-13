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

package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.mqtt.ClientData;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CommandLine.Command(name = "ls", aliases = "list",
        description = "List all connected clients with their respective identifiers")
public class ListClientsCommand implements Runnable, CliCommand {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-t"}, defaultValue = "false", description = "sort by creation time, newest first")
    private boolean sortByTime;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-U"}, defaultValue = "false", description = "do not sort")
    private boolean doNotSort;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--reverse"}, defaultValue = "false",
            description = "reverse order while sorting")
    private boolean reverse;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l", "--long"}, defaultValue = "false", description = "use a long listing format")
    private boolean longOutput;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--subscriptions"}, defaultValue = "false",
            description = "list subscribed topics of clients")
    private boolean listSubscriptions;

    @Inject
    //needed for pico cli - reflection code generation
    public ListClientsCommand() {
    }

    @Override
    public void run() {
        Logger.trace("Command {}", this);

        final List<ClientData> sortedClientData = getSortedClientData();

        final PrintWriter writer = ShellCommand.TERMINAL_WRITER;

        if (longOutput) {
            Objects.requireNonNull(writer).println("total " + sortedClientData.size());

            if (sortedClientData.size() == 0) {
                return;
            }

            final Set<MqttClient> clients =
                    sortedClientData.stream().map(ClientData::getClient).collect(Collectors.toSet());

            final int longestID = clients.stream()
                    .filter(c -> c.getConfig().getClientIdentifier().isPresent())
                    .map(c -> c.getConfig().getClientIdentifier().get().toString().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestHost =
                    clients.stream().map(c -> c.getConfig().getServerHost().length()).max(Integer::compareTo).orElse(0);

            final int longestState = clients.stream()
                    .map(c -> c.getConfig().getState().toString().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestVersion = clients.stream()
                    .map(c -> c.getConfig().getMqttVersion().toString().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestSSLVersion = clients.stream()
                    .map(c -> c.getConfig().getSslConfig().toString().length())
                    .max(Integer::compareTo)
                    .orElse("NO_SSL".length());

            final String format =
                    "%-" + longestState + "s " + "%02d:%02d:%02d " + "%-" + longestID + "s " + "%-" + longestHost +
                            "s " + "%5d " + "%-" + longestVersion + "s " + "%-" + longestSSLVersion + "s\n";

            for (final ClientData clientData : sortedClientData) {
                final MqttClient client = clientData.getClient();
                final LocalDateTime dateTime = clientData.getCreationTime();
                final String connectionState = client.getState().toString();

                writer.printf(
                        format,
                        connectionState,
                        dateTime.getHour(),
                        dateTime.getMinute(),
                        dateTime.getSecond(),
                        client.getConfig().getClientIdentifier().map(Object::toString).orElse(""),
                        client.getConfig().getServerHost(),
                        client.getConfig().getServerPort(),
                        client.getConfig().getMqttVersion().name(),
                        client.getConfig()
                                .getSslConfig()
                                .flatMap(ssl -> ssl.getProtocols().map(Objects::toString))
                                .orElse("NO_SSL"));

                if (listSubscriptions) {
                    writer.printf(" -subscribed topics: %s\n", clientData.getSubscribedTopics());
                }
            }
        } else {
            for (final ClientData clientData : sortedClientData) {
                Objects.requireNonNull(writer)
                        .println(clientData.getClient()
                                .getConfig()
                                .getClientIdentifier()
                                .map(Object::toString)
                                .orElse("") + "@" + clientData.getClient().getConfig().getServerHost());
                if (listSubscriptions) {
                    writer.printf(" -subscribed topics: %s\n", clientData.getSubscribedTopics());
                }
            }
        }
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "sortByTime=" + sortByTime + ", doNotSort=" + doNotSort +
                ", reverse=" + reverse + ", listSubscriptions" + listSubscriptions + ", longOutput=" + longOutput + '}';
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    public @NotNull List<ClientData> getSortedClientData() {
        final List<ClientData> sortedClientData = new ArrayList<>(MqttClientExecutor.getClientDataMap().values());

        if (doNotSort) {
            return sortedClientData;
        }

        Comparator<ClientData> comparator;
        if (sortByTime) {
            comparator = Comparator.comparing(ClientData::getCreationTime);
        } else {
            comparator = Comparator.comparing(clientData -> clientData.getClient()
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
}
