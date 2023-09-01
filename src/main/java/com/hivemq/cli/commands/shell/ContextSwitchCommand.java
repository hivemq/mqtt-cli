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

import com.hivemq.cli.commands.options.HelpOptions;
import com.hivemq.cli.mqtt.ClientKey;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "switch", description = "Switch the current context")
public class ContextSwitchCommand extends ShellContextCommand implements Callable<Integer> {

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

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    @Inject
    public ContextSwitchCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        if (contextName == null && identifier == null) {
            ShellCommand.usage(this);
            return 0;
        }

        if (contextName != null) {
            try {
                extractKeyFromContextName(contextName);
            } catch (final IllegalArgumentException ex) {
                LoggerUtils.logShellError("Unable to switch context", ex);
                return 1;
            }
        }

        final MqttClient client =
                mqttClientExecutor.getMqttClient(ClientKey.of(identifier, Objects.requireNonNull(host)));

        if (client != null) {
            updateContext(client);
        } else {
            Logger.error("Context {}@{} not found", identifier, host);
            return 1;
        }

        return 0;
    }

    private void extractKeyFromContextName(final String contextName) {
        final String[] context = contextName.split("@");

        if (context.length == 1) {
            identifier = context[0];
        } else if (context.length == 2) {
            identifier = context[0];
            host = context[1];
        } else {
            throw new IllegalArgumentException("Context name is not valid: " + contextName);
        }
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
                ", helpOptions=" +
                helpOptions +
                '}';
    }
}
