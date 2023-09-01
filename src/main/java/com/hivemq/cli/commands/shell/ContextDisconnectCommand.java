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

import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.HelpOptions;
import com.hivemq.cli.mqtt.ClientKey;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnects this MQTT client")
public class ContextDisconnectCommand extends ShellContextCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private final @NotNull DisconnectOptions disconnectOptions = new DisconnectOptions();

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    @Inject
    public ContextDisconnectCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        if (contextClient != null) {
            disconnectOptions.logUnusedDisconnectOptions(contextClient.getConfig().getMqttVersion());
        }

        try {
            if (disconnectOptions.isDisconnectAll()) {
                mqttClientExecutor.disconnectAllClients(disconnectOptions);
            } else if (disconnectOptions.getClientIdentifier() != null && disconnectOptions.getHost() != null) {
                final ClientKey clientKey =
                        ClientKey.of(disconnectOptions.getClientIdentifier(), disconnectOptions.getHost());
                mqttClientExecutor.disconnect(clientKey, disconnectOptions);
            } else if (contextClient != null) {
                mqttClientExecutor.disconnect(contextClient, disconnectOptions);
            }
        } catch (final Exception ex) {
            LoggerUtils.logShellError("Unable to disconnect", ex);
            return 1;
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ContextDisconnectCommand{" +
                "disconnectOptions=" +
                disconnectOptions +
                ", helpOptions=" +
                helpOptions +
                '}';
    }
}
