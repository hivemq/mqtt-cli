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

package com.hivemq.cli.mqtt;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.shell.ShellCommand;
import com.hivemq.cli.commands.shell.ShellContextCommand;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.Objects;

public class ContextClientDisconnectListener implements MqttClientDisconnectedListener {

    @Override
    public void onDisconnected(final @NotNull MqttClientDisconnectedContext context) {
        if (context.getSource() != MqttDisconnectSource.USER) {
            final Throwable cause = context.getCause();

            Logger.debug(cause,
                    "{} DISCONNECTED {}",
                    LoggerUtils.getClientPrefix(context.getClientConfig()),
                    Throwables.getRootCause(cause).getMessage());

            // If the currently active shell client gets disconnected from the server prompt the user to enter
            if (contextEqualsShellContext(context)) {
                Logger.error(cause, Throwables.getRootCause(cause).getMessage());
                ShellContextCommand.removeContext();
                Objects.requireNonNull(ShellCommand.TERMINAL_WRITER).printf("Press ENTER to resume: ");
                ShellCommand.TERMINAL_WRITER.flush();
            }
        } else if (contextEqualsShellContext(context)) {
            ShellContextCommand.removeContext();
        }
        MqttClientExecutor.getClientDataMap().remove(ClientKey.of(context.getClientConfig()));
    }

    private boolean contextEqualsShellContext(final @NotNull MqttClientDisconnectedContext context) {
        final MqttClientConfig clientConfig = context.getClientConfig();
        final MqttClientConfig shellClientConfig =
                Objects.requireNonNull(ShellContextCommand.contextClient).getConfig();

        return clientConfig.getClientIdentifier().equals(shellClientConfig.getClientIdentifier()) &&
                clientConfig.getServerHost().equals(shellClientConfig.getServerHost());
    }
}
