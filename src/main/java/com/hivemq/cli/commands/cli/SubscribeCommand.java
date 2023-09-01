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
import com.hivemq.cli.commands.options.SubscribeOptions;
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

@CommandLine.Command(name = "sub",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     aliases = "subscribe",
                     description = "Subscribe an MQTT client to a list of topics.")
public class SubscribeCommand implements Callable<Integer> {

    private static final int IDLE_TIME = 5000;
    private final @NotNull MqttClientExecutor mqttClientExecutor;
    private @Nullable MqttClient subscribeClient;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)")
    private boolean logToLogfile;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-no-oc", "--no-outputToConsole"},
                        hidden = true,
                        negatable = true,
                        defaultValue = "true",
                        description = "The received messages will be written to the console (default: true)")
    private void printToSTDOUT(final boolean printToSTDOUT) {
        subscribeOptions.setPrintToSTDOUT(printToSTDOUT);
    }

    @CommandLine.Mixin
    private final @NotNull ConnectOptions connectOptions = new ConnectOptions();

    @CommandLine.Mixin
    private final @NotNull SubscribeOptions subscribeOptions = new SubscribeOptions();

    @CommandLine.Mixin
    private final @NotNull DebugOptions debugOptions = new DebugOptions();

    @CommandLine.Mixin
    private final @NotNull HelpOptions helpOptions = new HelpOptions();

    @Inject
    public SubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
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
        subscribeOptions.setDefaultOptions();
        subscribeOptions.logUnusedOptions(connectOptions.getVersion());
        subscribeOptions.arrangeQosToMatchTopics();

        if (subscribeOptions.isOutputFileInvalid(subscribeOptions.getOutputFile())) {
            return 1;
        }

        try {
            subscribeClient = mqttClientExecutor.connect(connectOptions, subscribeOptions);
        } catch (final Exception exception) {
            LoggerUtils.logCommandError("Unable to connect", exception, debugOptions);
            return 1;
        }

        try {
            mqttClientExecutor.subscribe(subscribeClient, subscribeOptions);
        } catch (final Exception exception) {
            LoggerUtils.logCommandError("Unable to subscribe", exception, debugOptions);
            return 1;
        }

        try {
            stay();
        } catch (final InterruptedException exception) {
            LoggerUtils.logCommandError("Unable to stay", exception, debugOptions);
            return 1;
        }

        return 0;
    }

    private void stay() throws InterruptedException {
        while (Objects.requireNonNull(subscribeClient).getState().isConnectedOrReconnect()) {
            Thread.sleep(IDLE_TIME);
        }
    }

    @Override
    public @NotNull String toString() {
        return "SubscribeCommand{" +
                "mqttClientExecutor=" +
                mqttClientExecutor +
                ", subscribeClient=" +
                subscribeClient +
                ", logToLogfile=" +
                logToLogfile +
                ", connectOptions=" +
                connectOptions +
                ", subscribeOptions=" +
                subscribeOptions +
                ", debugOptions=" +
                debugOptions +
                ", helpOptions=" + helpOptions +
                '}';
    }
}
