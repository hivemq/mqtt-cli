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
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(sortOptions = false,
        name = "> ",
        description = "In context mode all MQTT commands relate to the currently active client.",
        synopsisHeading = "%n@|bold Usage|@:  ",
        synopsisSubcommandLabel = "{ pub | sub | unsub | dis | switch | ls | cls | exit }",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options|@:%n",
        commandListHeading = "%n@|bold Commands|@:%n",
        separator = " ")

public class ShellContextCommand implements Runnable, CliCommand {

    public static @Nullable MqttClient contextClient;
    MqttClientExecutor mqttClientExecutor;

    //needed for pico cli - reflection code generation
    public ShellContextCommand() {
        this(null);
    }

    @Inject
    public ShellContextCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    static void updateContext(final @Nullable MqttClient client) {
        if (client != null && client.getConfig().getState().isConnectedOrReconnect()) {
            contextClient = client;
            ShellCommand.readFromContext();
        }
    }

    public static void removeContext() {
        contextClient = null;
        ShellCommand.readFromShell();
    }

    @Override
    public void run() {
        ShellCommand.TERMINAL_WRITER.println(ShellCommand.getUsageMessage());
    }

    public String getKey() {
        return "client {" +
                "identifier='" + contextClient.getConfig().getClientIdentifier().get() + '\'' +
                ", host='" + contextClient.getConfig().getServerHost() + '\'' +
                '}';
    }

    public String getIdentifier() {
        return contextClient.getConfig().getClientIdentifier().get().toString();
    }

    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }
}
