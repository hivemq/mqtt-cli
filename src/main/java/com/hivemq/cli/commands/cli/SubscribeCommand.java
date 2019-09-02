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

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

@CommandLine.Command(name = "sub",
        aliases = "subscribe",
        description = "Subscribe an mqtt client to a list of topics")

public class SubscribeCommand extends ConnectCommand implements MqttAction, Subscribe {

    public static final int IDLE_TIME = 5000;
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of service for the corresponding topics (default for all: 0)")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-sup", "--subscribeUserProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the subscribe message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')")
    @Nullable Mqtt5UserProperties subscribeUserProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "A file to which the received publish messages will be written")
    @Nullable
    private File receivedMessagesFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false", description = "The received messages will be written to the console (default: false)")
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64 (default: false)")
    private boolean base64;


    @Inject
    public SubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        super(mqttClientExecutor);

    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(final MqttQos[] qos) {
        this.qos = qos;
    }

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

    public Mqtt5UserProperties getSubscribeUserProperties() {
        return subscribeUserProperties;
    }

    public void setSubscribeUserProperties(final Mqtt5UserProperties subscribeUserProperties) {
        this.subscribeUserProperties = subscribeUserProperties;
    }

    @Override
    public void run() {


        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        handleConnectOptions();

        logUnusedSubscribeOption();

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.error(ex);
            }
            Logger.error(ex.getMessage());
        }

        if (receivedMessagesFile == null && !printToSTDOUT) {
            printToSTDOUT = true;
        }
        try {
            stay();
        } catch (final InterruptedException e) {
            if (isDebug()) {
                Logger.debug(e);
            }
            Logger.error(e.getMessage());
        }


    }

    private void logUnusedSubscribeOption() {
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
    public String toString() {
        return "Subscribe:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", userProperties=" + subscribeUserProperties +
                ", toFile=" + receivedMessagesFile +
                ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 +
                ", Connect:: {" + connectOptions() + "}" +
                '}';
    }


}
