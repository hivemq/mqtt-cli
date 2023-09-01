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

package com.hivemq.cli.commands.cli;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DebugOptions;
import com.hivemq.cli.commands.options.HelpOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "pub",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     aliases = "publish",
                     description = "Publish a message to a list of topics.")
public class PublishCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)")
    private boolean logToLogfile;

    @CommandLine.Mixin
    private final @NotNull ConnectOptions connectOptions = new ConnectOptions();

    @CommandLine.Mixin
    private final @NotNull PublishOptions publishOptions = new PublishOptions();

    @CommandLine.Mixin
    private final @NotNull DebugOptions debugOptions = new DebugOptions();

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    private final @NotNull MqttClientExecutor mqttClientExecutor;

    @Inject
    public PublishCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @Override
    public @NotNull Integer call() {
        String logLevel = "warn";
        if (debugOptions.isDebug()) {
            logLevel = "debug";
        }
        if (debugOptions.isVerbose()) {
            logLevel = "trace";
        }
        LoggerUtils.setupConsoleLogging(logToLogfile, logLevel);

        Logger.trace("Command {}", this);

        connectOptions.setDefaultOptions();
        connectOptions.logUnusedOptions();
        publishOptions.logUnusedOptions(connectOptions.getVersion());
        publishOptions.arrangeQosToMatchTopics();

        final MqttClient client;
        try {
            client = mqttClientExecutor.connect(connectOptions, null);
        } catch (final Exception exception) {
            LoggerUtils.logCommandError("Unable to connect", exception, debugOptions);
            return 1;
        }

        try {
            mqttClientExecutor.publish(client, publishOptions);
        } catch (final Exception exception) {
            LoggerUtils.logCommandError("Unable to publish", exception, debugOptions);
            return 1;
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "PublishCommand{" +
                "logToLogfile=" +
                logToLogfile +
                ", connectOptions=" +
                connectOptions +
                ", publishOptions=" +
                publishOptions +
                ", debugOptions=" +
                debugOptions +
                ", helpOptions=" + helpOptions +
                ", mqttClientExecutor=" +
                mqttClientExecutor +
                '}';
    }
}
