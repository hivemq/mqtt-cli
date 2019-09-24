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

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

@CommandLine.Command(name = "sub",
        versionProvider = MqttCLIMain.CLIVersionProvider.class,
        aliases = "subscribe",
        description = "Subscribe an mqtt client to a list of topics",
        abbreviateSynopsis = false)

public class SubscribeCommand extends AbstractConnectFlags implements MqttAction, Subscribe {

    private final MqttClientExecutor mqttClientExecutor;

    private MqttClientSslConfig sslConfig;

    public static final int IDLE_TIME = 5000;

    //needed for pico cli - reflection code generation
    public SubscribeCommand() {
        this(null);
    }

    @Inject
    public SubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;

    }

    @CommandLine.Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to", order = 1)
    @NotNull
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "2", description = "Quality of service for the corresponding topics (default for all: 0)", order = 1)
    @NotNull
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the subscribe message", order = 1)
    @Nullable Mqtt5UserProperty[] userProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "A file to which the received publish messages will be written", order = 1)
    @Nullable
    private File receivedMessagesFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false", description = "The received messages will be written to the console (default: false)", order = 1)
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64 (default: false)", order = 1)
    private boolean base64;

    @Override
    public void run() {

        LoggingContext.put("identifier", "SUBSCRIBE");


        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        setDefaultOptions();

        sslConfig = buildSslConfig();

        logUnusedOptions();

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(this);
        }
        catch (final Exception ex) {
            if (ex instanceof ConnectionFailedException) {
                LoggingContext.put("identifier", "CONNECT");
            }
            else {
                LoggingContext.put("identifier", "PUBLISH");
            }
            if (isVerbose()) {
                Logger.trace(ex);
            }
            else if (isDebug()) {
                Logger.debug(ex.getMessage());
            }
            Logger.error(MqttUtils.getRootCause(ex).getMessage());
        }

        if (receivedMessagesFile == null && !printToSTDOUT) {
            printToSTDOUT = true;
        }
        try {
            stay();
        }
        catch (final InterruptedException ex) {
            if (isVerbose()) {
                Logger.trace(ex);
            }
            else if (isDebug()) {
                Logger.debug(ex.getMessage());
            }
            Logger.error(MqttUtils.getRootCause(ex).getMessage());
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
        while (mqttClientExecutor.isConnected(this)) {
            Thread.sleep(IDLE_TIME);
        }
        if (isVerbose()) {
            Logger.trace("Client disconnected.");
        }
    }

    @Override
    public void setDefaultOptions() {
        super.setDefaultOptions();

        if (receivedMessagesFile == null && PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE != null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'toFile' to {}", PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);
            }
            receivedMessagesFile = new File(PropertiesUtils.DEFAULT_SUBSCRIBE_OUTPUT_FILE);
        }

    }

    @Override
    public String toString() {
        return "Subscribe:: {" +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", userProperties=" + userProperties +
                ", toFile=" + receivedMessagesFile +
                ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 +
                ", Connect:: {" + commonOptions() + "}" +
                '}';
    }


    @NotNull
    @Override
    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    @NotNull
    @Override
    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(final MqttQos[] qos) {
        this.qos = qos;
    }

    @Nullable
    @Override
    public File getReceivedMessagesFile() {
        return receivedMessagesFile;
    }

    public void setReceivedMessagesFile(@Nullable final File receivedMessagesFile) {
        this.receivedMessagesFile = receivedMessagesFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public void setPrintToSTDOUT(final boolean printToSTDOUT) {
        this.printToSTDOUT = printToSTDOUT;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(final boolean base64) {
        this.base64 = base64;
    }

    @Nullable
    @Override
    public Mqtt5UserProperties getUserProperties() {

        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void setUserProperties(@Nullable final Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
    }


    @Nullable
    @Override
    public MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(final MqttClientSslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }
}
