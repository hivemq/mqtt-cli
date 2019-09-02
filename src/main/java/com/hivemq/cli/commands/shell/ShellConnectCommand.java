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

import com.hivemq.cli.commands.cli.ConnectCommand;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "con",
        aliases = "connect",
        description = "Connects an mqtt client",
        abbreviateSynopsis = true)

public class ShellConnectCommand extends ConnectCommand {

    @Inject
    public ShellConnectCommand(@NotNull final MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @Override
    public void run() {
        super.run();
        ShellContextCommand.updateContext(client);
    }

    @Override
    public String toString() {
        return "ShellConnectCommand:: " + super.toString();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }
}
