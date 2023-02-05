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

import com.hivemq.cli.mqtt.clients.CliMqttClient;
import com.hivemq.cli.mqtt.clients.ShellClients;
import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "switch", description = "Switch the current context", mixinStandardHelpOptions = true)
public class ContextSwitchCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Parameters(index = "0", arity = "0..1", description = "The name of the context, e.g. client@localhost")
    private @Nullable String contextName;

    @CommandLine.Option(names = {"-i", "--identifier"},
                        description = "The client identifier UTF-8 String (default randomly generated string)")
    private @Nullable String identifier;

    @CommandLine.Option(names = {"-h", "--host"},
                        defaultValue = "localhost",
                        description = "The hostname of the message broker (default 'localhost')")
    private @Nullable String host;

    private final @NotNull ShellClients shellClients;

    @Inject
    public ContextSwitchCommand(final @NotNull ShellClients shellClients) {
        this.shellClients = shellClients;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {} ", this);

        if (contextName == null && identifier == null) {
            ShellCommand.usage(this);
            return 0;
        }

        if (contextName != null) {
            final String[] context = contextName.split("@");
            if (context.length == 1) {
                identifier = context[0];
            } else if (context.length == 2) {
                identifier = context[0];
                host = context[1];
            } else {
                LoggerUtils.logShellError("Unable to switch context", new IllegalArgumentException("Context name is not valid: " + contextName));
                return 1;
            }
        }

        final CliMqttClient client = shellClients.getClient(identifier, host);

        if (client != null) {
            shellClients.updateContextClient(client);
        } else {
            Logger.error("Context {}@{} not found", identifier, host);
            return 1;
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ContextSwitchCommand{" +
                "contextName='" +
                contextName +
                '\'' +
                ", identifier='" +
                identifier +
                '\'' +
                ", host='" +
                host +
                '\'' +
                '}';
    }
}
