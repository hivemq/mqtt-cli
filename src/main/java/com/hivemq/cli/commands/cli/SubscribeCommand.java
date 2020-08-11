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
package com.hivemq.cli.commands.cli;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(name = "sub",
        versionProvider = MqttCLIMain.CLIVersionProvider.class,
        aliases = "subscribe",
        description = "Subscribe an mqtt client to a list of topics.",
        abbreviateSynopsis = false)

public class SubscribeCommand extends AbstractConnectFlags implements MqttAction, Subscribe {

    private final MqttClientExecutor mqttClientExecutor;
    private final DefaultCLIProperties defaultCLIProperties;
    private MqttClient subscribeClient;

    private MqttClientSslConfig sslConfig;

    public static final int IDLE_TIME = 5000;

    //needed for pico cli - reflection code generation
    public SubscribeCommand() {
        this(null, null);
    }

    @Inject
    public SubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor,
                            final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.mqttClientExecutor = mqttClientExecutor;
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @CommandLine.Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to", order = 1)
    @NotNull private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "2", description = "Quality of service for the corresponding topics (default for all: 2)", order = 1)
    @NotNull private MqttQos[] qos;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the subscribe message", order = 1)
    @Nullable private Mqtt5UserProperty[] userProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "A file to which the received publish messages will be written", order = 1)
    @Nullable private File publishFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, hidden = true, defaultValue = "true", description = "The received messages will be written to the console (default: true)", order = 1)
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64 (default: false)", order = 1)
    private boolean base64;

    @CommandLine.Option(names = {"-J", "--jsonOutput"}, defaultValue = "false", description = "Print the received publishes in pretty JSON format", order = 1)
    private boolean jsonOutput;

    @CommandLine.Option(names = {"-T", "--showTopics"}, defaultValue = "false", description = "Prepend the specific topic name to the received publish", order = 1)
    private boolean showTopics;

    @CommandLine.Option(names = {"-l"}, defaultValue = "false", description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)", order = 1)
    private boolean logToLogfile;

    @Override
    public void run() {

        String logLevel = "warn";
        if (isDebug()) logLevel = "debug";
        else if (isVerbose()) logLevel = "trace";
        LoggerUtils.setupConsoleLogging(logToLogfile, logLevel);

        setDefaultOptions();
        sslConfig = buildSslConfig();

        Logger.trace("Command {} ", this);

        logUnusedOptions();

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            subscribeClient = mqttClientExecutor.subscribe(this);
        }
        catch (final ConnectionFailedException cex) {
            Logger.error(cex, cex.getCause().getMessage());
            return;
        }
        catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
            return;
        }

        try {
            stay();
        }
        catch (final InterruptedException ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }


    }

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Subscribe user properties were set but are unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    private void stay() throws InterruptedException {
        while (subscribeClient.getState().isConnectedOrReconnect()) {
            Thread.sleep(IDLE_TIME);
        }
    }

    @Override
    public void setDefaultOptions() {
        super.setDefaultOptions();

        if (publishFile == null && defaultCLIProperties.getClientSubscribeOutputFile() != null) {
            Logger.trace("Setting value of 'toFile' to {}", defaultCLIProperties.getClientSubscribeOutputFile());
            publishFile = new File(defaultCLIProperties.getClientSubscribeOutputFile());
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                connectOptions() +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 +
                ", jsonOutput=" + jsonOutput +
                ", showTopics=" + showTopics +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") +
                (publishFile != null ? (", publishFile=" + publishFile.getAbsolutePath()) : "") +
                '}';
    }


    @NotNull
    @Override
    public String[] getTopics() {
        return topics;
    }

    @NotNull
    @Override
    public MqttQos[] getQos() {
        return qos;
    }

    @Nullable
    @Override
    public File getPublishFile() {
        return publishFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public boolean isBase64() { return base64; }

    public boolean isJsonOutput() { return jsonOutput; }

    public boolean showTopics() { return showTopics; }

    @Nullable
    @Override
    public Mqtt5UserProperties getUserProperties() { return MqttUtils.convertToMqtt5UserProperties(userProperties); }

    public void setUserProperties(@Nullable final Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
    }

    @Nullable
    @Override
    public MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }

}
