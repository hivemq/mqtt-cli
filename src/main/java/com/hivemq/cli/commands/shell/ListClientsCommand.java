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

import com.hivemq.cli.mqtt.clients.CliMqttClient;
import com.hivemq.cli.mqtt.clients.ShellClients;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "ls",
                     aliases = "list",
                     description = "List all connected clients with their respective identifiers",
                     mixinStandardHelpOptions = true)
public class ListClientsCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-t"}, defaultValue = "false", description = "sort by creation time, newest first")
    private boolean sortByTime;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-U"}, defaultValue = "false", description = "do not sort")
    private boolean doNotSort;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--reverse"},
                        defaultValue = "false",
                        description = "reverse order while sorting")
    private boolean reverse;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l", "--long"}, defaultValue = "false", description = "use a long listing format")
    private boolean longOutput;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--subscriptions"},
                        defaultValue = "false",
                        description = "list subscribed topics of clients")
    private boolean listSubscriptions;
    private final @NotNull ShellClients shellClients;

    @Inject
    public ListClientsCommand(final @NotNull ShellClients shellClients) {
        this.shellClients = shellClients;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final List<CliMqttClient> sortedClients = getSortedClientData();

        final PrintWriter writer = ShellCommand.TERMINAL_WRITER;

        if (longOutput) {
            Objects.requireNonNull(writer).println("total " + sortedClients.size());

            if (sortedClients.size() == 0) {
                return 0;
            }

            final int longestId = sortedClients.stream()
                    .map(client -> client.getClientIdentifier().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestHost = sortedClients.stream()
                    .map(client -> client.getServerHost().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestState = sortedClients.stream()
                    .map(client -> client.getState().toString().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestVersion = sortedClients.stream()
                    .map(client -> client.getMqttVersion().toString().length())
                    .max(Integer::compareTo)
                    .orElse(0);

            final int longestSslVersion = sortedClients.stream()
                    .map(client -> client.getSslProtocols().length())
                    .max(Integer::compareTo)
                    .orElse("NO_SSL".length());

            final String format = "%-" +
                    longestState +
                    "s " +
                    "%02d:%02d:%02d " +
                    "%-" +
                    longestId +
                    "s " +
                    "%-" +
                    longestHost +
                    "s " +
                    "%5d " +
                    "%-" +
                    longestVersion +
                    "s " +
                    "%-" +
                    longestSslVersion +
                    "s\n";

            for (final CliMqttClient client : sortedClients) {
                final LocalDateTime dateTime = client.getConnectedAt();
                final String connectionState = client.getState().toString();

                writer.printf(format,
                        connectionState,
                        dateTime.getHour(),
                        dateTime.getMinute(),
                        dateTime.getSecond(),
                        client.getClientIdentifier(),
                        client.getServerHost(),
                        client.getServerPort(),
                        client.getMqttVersion().name(),
                        client.getSslProtocols());

                if (listSubscriptions) {
                    writer.printf(" -subscribed topics: %s\n", client.getSubscribedTopics());
                }
            }
        } else {
            for (final CliMqttClient client : sortedClients) {
                Objects.requireNonNull(writer)
                        .println(client.getClientIdentifier() + "@" + client.getServerHost());
                if (listSubscriptions) {
                    writer.printf(" -subscribed topics: %s\n", client.getSubscribedTopics());
                }
            }
        }

        return 0;
    }

    public @NotNull List<CliMqttClient> getSortedClientData() {
        Comparator<CliMqttClient> comparator;

        if (doNotSort) {
            comparator = (client1, client2) -> 0; // No-op comparator
        } else if (sortByTime) {
            comparator = Comparator.comparing(CliMqttClient::getConnectedAt);
        } else {
            comparator = Comparator.comparing(CliMqttClient::getClientIdentifier);
        }

        if (reverse) {
            comparator = comparator.reversed();
        }

        return shellClients.listClients(comparator);
    }

    @Override
    public @NotNull String toString() {
        return "ListClientsCommand{" +
                "sortByTime=" +
                sortByTime +
                ", doNotSort=" +
                doNotSort +
                ", reverse=" +
                reverse +
                ", longOutput=" +
                longOutput +
                ", listSubscriptions=" +
                listSubscriptions +
                '}';
    }
}
