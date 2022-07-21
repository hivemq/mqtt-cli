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
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DebugOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "con", aliases = "connect", description = "Connect an MQTT client",
        abbreviateSynopsis = true)
public class ShellConnectCommand implements Callable<Integer> {

    private final @NotNull MqttClientExecutor mqttClientExecutor;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Mixin
    private final @NotNull ConnectOptions connectOptions = new ConnectOptions();


    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ShellConnectCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ShellConnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    public Integer call() {

        Logger.trace("Command {} ", this);

        connectOptions.setDefaultOptions();

        final MqttClient client;
        try {
            client = mqttClientExecutor.connect(connectOptions);
        } catch (final ConnectionFailedException cex) {
            Logger.error(cex, "Unable to connect: {}", cex.getCause().getMessage());
            return 1;
        } catch (final Exception ex) {
            Logger.error(ex, "Unable to connect: {}", Throwables.getRootCause(ex).getMessage());
            return 1;
        }

        ShellContextCommand.updateContext(client);

        return 0;
    }

    @Override
    public String toString() {
        return "ShellConnectCommand{" + "usageHelpRequested=" + usageHelpRequested + ", connectOptions=" +
                connectOptions + '}';
    }
}
