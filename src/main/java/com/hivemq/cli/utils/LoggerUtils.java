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
package com.hivemq.cli.utils;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.commands.shell.ShellContextCommand;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Objects;
import java.util.Optional;

public class LoggerUtils {

    public static void logOnRightLevels(final @NotNull CliCommand cmd, final @NotNull Exception ex) {
        if (cmd.isVerbose()) {
            Logger.trace(ex);
        }
        else if (cmd.isDebug()) {
            Logger.debug(ex.getMessage());
        }
        Logger.error(MqttUtils.getRootCause(ex).getMessage());
    }

    public static void logWithCurrentContext(final @NotNull CliCommand cmd, final @NotNull Exception ex) {
        if (cmd.isVerbose()) {
            Logger.trace("{} : ", getShellContextClientPrefix(), ex);
        }
        else if (cmd.isDebug()) {
            Logger.debug("{} : ", getShellContextClientPrefix(), ex.getMessage());
        }
        Logger.error(MqttUtils.getRootCause(ex).getMessage());
    }

    public static String getShellContextClientPrefix() {
        if (ShellContextCommand.contextClient != null) {
            final MqttClientConfig config = ShellContextCommand.contextClient.getConfig();
            return getClientPrefix(config);
        }
        else {
            return "";
        }
    }

    public static String getClientPrefix(final @NotNull MqttClientConfig config) {
        Optional<MqttClientIdentifier> optId = config.getClientIdentifier();
        String id = optId.map(Objects::toString).orElse("UNKNOWN");
        if (id.isEmpty()) {
            id = "UNKNOWN";
        }
        return "Client '" +
                id +
                "@" +
                config.getServerHost() +
                "'";
    }
}
