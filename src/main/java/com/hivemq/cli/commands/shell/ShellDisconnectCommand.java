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

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.options.DefaultOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.mqtt.clients.ShellClients;
import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnect an MQTT client")
public class ShellDisconnectCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private final @NotNull DisconnectOptions disconnectOptions = new DisconnectOptions();

    @CommandLine.Mixin
    private final @NotNull DefaultOptions defaultOptions = new DefaultOptions();

    private final @NotNull ShellClients shellClients;
    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    @Inject
    ShellDisconnectCommand(
            final @NotNull ShellClients shellClients,
            final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.shellClients = shellClients;
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public @NotNull Integer call() {
        if (disconnectOptions.getHost() == null) {
            disconnectOptions.setHost(defaultCLIProperties.getHost());
        }
        Logger.trace("Command {} ", this);

        try {
            if (disconnectOptions.isDisconnectAll()) {
                shellClients.disconnectAllClients(disconnectOptions);
            } else {
                if (disconnectOptions.getClientIdentifier() == null) {
                    Logger.error("Missing required option '--identifier=<identifier>'");
                    return 1;
                }
                shellClients.disconnect(disconnectOptions);
            }
        } catch (final Exception ex) {
            LoggerUtils.logShellError("Unable to disconnect", ex);
            return 1;
        }
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ShellDisconnectCommand{" +
                "disconnectOptions=" +
                disconnectOptions +
                ", defaultOptions=" +
                defaultOptions +
                ", defaultCLIProperties=" +
                defaultCLIProperties +
                '}';
    }
}
