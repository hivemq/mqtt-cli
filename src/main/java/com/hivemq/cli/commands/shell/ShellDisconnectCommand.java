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

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.mqtt.ClientKey;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnect an MQTT client")
public class ShellDisconnectCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Mixin
    private final @NotNull DisconnectOptions disconnectOptions = new DisconnectOptions();

    private final @NotNull MqttClientExecutor mqttClientExecutor;
    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ShellDisconnectCommand() {
        //noinspection ConstantConditions
        this(null, null);
    }

    @Inject
    ShellDisconnectCommand(
            final @NotNull MqttClientExecutor mqttClientExecutor,
            final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.mqttClientExecutor = mqttClientExecutor;
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public Integer call() {
        if (disconnectOptions.getHost() == null) {
            disconnectOptions.setHost(defaultCLIProperties.getHost());
        }
        Logger.trace("Command {} ", this);

        try {
            if (disconnectOptions.isDisconnectAll()) {
                mqttClientExecutor.disconnectAllClients(disconnectOptions);
            } else {
                if (disconnectOptions.getClientIdentifier() == null) {
                    Logger.error("Missing required option '--identifier=<identifier>'");
                    return 1;
                }
                final ClientKey clientKey = ClientKey.of(disconnectOptions.getClientIdentifier(), disconnectOptions.getHost());
                mqttClientExecutor.disconnect(clientKey, disconnectOptions);
            }
        } catch (final Exception ex) {
            Logger.error(ex, "Unable to disconnect: {}", Throwables.getRootCause(ex).getMessage());
            return 1;
        }

        return 0;
    }

    @Override
    public String toString() {
        return "ShellDisconnectCommand{" + "usageHelpRequested=" + usageHelpRequested + ", disconnectOptions=" +
                disconnectOptions + '}';
    }
}
