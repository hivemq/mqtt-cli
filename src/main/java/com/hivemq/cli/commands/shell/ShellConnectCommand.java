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

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.HelpOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "con",
                     aliases = "connect",
                     description = "Connect an MQTT client",
                     abbreviateSynopsis = true)
public class ShellConnectCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private final @NotNull ConnectOptions connectOptions = new ConnectOptions();

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    private final @NotNull MqttClientExecutor mqttClientExecutor;

    @Inject
    public ShellConnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        connectOptions.setDefaultOptions();
        connectOptions.logUnusedOptions();

        final MqttClient client;
        try {
            client = mqttClientExecutor.connect(connectOptions);
        } catch (final Exception exception) {
            LoggerUtils.logShellError("Unable to connect", exception);
            return 1;
        }

        ShellContextCommand.updateContext(client);

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ShellConnectCommand{" +
                "connectOptions=" +
                connectOptions +
                ", helpOptions=" + helpOptions +
                ", mqttClientExecutor=" +
                mqttClientExecutor +
                '}';
    }
}
