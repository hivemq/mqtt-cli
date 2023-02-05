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
package com.hivemq.cli.mqtt.clients;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.shell.ShellCommand;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class ShellClients {

    private final @NotNull Map<String, Map<String, CliMqttClient>> hostToClients = new ConcurrentHashMap<>();
    private final @NotNull CopyOnWriteArrayList<Consumer<CliMqttClient>> contextClientChangedListeners = new CopyOnWriteArrayList<>();
    private @Nullable CliMqttClient contextClient;

    @Inject
    public ShellClients() {
    }

    public @NotNull CliMqttClient connect(final @NotNull ConnectOptions connectOptions) throws Exception {
        final CliMqttClient client = CliMqttClient.connectWith(connectOptions)
                .addDisconnectedListener(new UpdateContextClientDisconnectedListener())
                .send();
        hostToClients.putIfAbsent(client.getServerHost(), new ConcurrentHashMap<>());
        final Map<String, CliMqttClient> idToClient = hostToClients.get(client.getServerHost());
        idToClient.put(client.getClientIdentifier(), client);
        return client;
    }

    public void disconnectAllClients(final @NotNull DisconnectOptions disconnectOptions) {
        hostToClients.values()
                .stream()
                .flatMap(idToClient -> idToClient.values().stream())
                .forEach(client -> client.disconnect(disconnectOptions));
        hostToClients.clear();
    }

    public void disconnect(final @NotNull DisconnectOptions disconnectOptions) {
        if (disconnectOptions.getClientIdentifier() == null) {
            return;
        }
        final CliMqttClient client = getClient(disconnectOptions.getClientIdentifier(), disconnectOptions.getHost());
        if (client == null) {
            return;
        }
        client.disconnect(disconnectOptions);
    }

    public @Nullable CliMqttClient getClient(final @NotNull String identifier, final @Nullable String serverHost) {
        if (serverHost != null) {
            final Map<String, CliMqttClient> idToClient =
                    hostToClients.getOrDefault(serverHost, Collections.emptyMap());
            return idToClient.get(identifier);
        } else {
            return hostToClients.values()
                    .stream()
                    .flatMap(idToClient -> idToClient.values().stream())
                    .filter(client -> client.getClientIdentifier().equals(identifier))
                    .findFirst()
                    .orElse(null);
        }
    }

    public @NotNull List<CliMqttClient> listClients(final @NotNull Comparator<CliMqttClient> comparator) {
        return hostToClients.values()
                .stream()
                .flatMap(idToClient -> idToClient.values().stream())
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public void addContextClientChangedListener(final @NotNull Consumer<CliMqttClient> consumer) {
        this.contextClientChangedListeners.add(consumer);
    }

    public void updateContextClient(final @NotNull CliMqttClient client) {
        if (client.isConnected()) {
            this.contextClient = client;
            for (final Consumer<CliMqttClient> contextClientChangedListener : contextClientChangedListeners) {
                contextClientChangedListener.accept(client);
            }
        }
    }

    public void removeContextClient() {
        this.contextClient = null;
        for (final Consumer<CliMqttClient> contextClientChangedListener : contextClientChangedListeners) {
            contextClientChangedListener.accept(null);
        }
    }

    public @Nullable CliMqttClient getContextClient() {
        return contextClient;
    }

    private boolean isContextClient(final @NotNull String clientId, final @NotNull String host) {
        if (contextClient == null) {
            return false;
        }
        return contextClient.getClientIdentifier().equals(clientId) && contextClient.getServerHost().equals(host);
    }

    private class UpdateContextClientDisconnectedListener implements MqttClientDisconnectedListener {

        @Override
        public void onDisconnected(@NotNull final MqttClientDisconnectedContext context) {
            final String clientId = context.getClientConfig().getClientIdentifier().map(Objects::toString).orElse("");
            final String serverHost = context.getClientConfig().getServerHost();
            if (context.getSource() != MqttDisconnectSource.USER) {
                if (isContextClient(clientId, serverHost)) {
                    Logger.error(context.getCause(), Throwables.getRootCause(context.getCause()).getMessage());
                    removeContextClient();
                    Objects.requireNonNull(ShellCommand.TERMINAL_WRITER).printf("Press ENTER to resume: ");
                    ShellCommand.TERMINAL_WRITER.flush();
                }
            } else if (isContextClient(clientId, serverHost)) {
                removeContextClient();
            }

            final Map<String, CliMqttClient> idToClient =
                    hostToClients.getOrDefault(serverHost, Collections.emptyMap());
            idToClient.remove(clientId);
        }
    }
}
