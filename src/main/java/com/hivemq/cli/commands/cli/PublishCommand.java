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
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.converters.*;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import picocli.CommandLine;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;

@CommandLine.Command(name = "pub",
        versionProvider = MqttCLIMain.CLIVersionProvider.class,
        aliases = "publish",
        description = "Publish a message to a list of topics",
        abbreviateSynopsis = false)

public class PublishCommand extends AbstractConnectFlags implements MqttAction, Publish {

    private final MqttClientExecutor mqttClientExecutor;

    private MqttClientSslConfig sslConfig;

    //needed for pico cli - reflection code generation
    public PublishCommand() {
        this(null);
    }

    @Inject
    public PublishCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;

    }

    @CommandLine.Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to", order = 1)
    @NotNull
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of service for the corresponding topic (default for all: 0)", order = 1)
    @NotNull
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-m", "--message"}, converter = ByteBufferConverter.class, required = true, description = "The message to publish", order = 1)
    @NotNull
    private ByteBuffer message;

    @CommandLine.Option(names = {"-r", "--retain"}, negatable = true, description = "The message will be retained (default: false)", order = 1)
    @Nullable
    private Boolean retain;

    @CommandLine.Option(names = {"-e", "--messageExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the publish message in seconds (default: no message expiry)", order = 1)
    @Nullable
    private Long messageExpiryInterval;

    @CommandLine.Option(names = {"-pf", "--payloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class, description = "The payload format indicator of the publish message", order = 1)
    @Nullable
    private Mqtt5PayloadFormatIndicator payloadFormatIndicator;

    @CommandLine.Option(names = {"-ct", "--contentType"}, description = "A description of publish message's content", order = 1)
    @Nullable
    private String contentType;

    @CommandLine.Option(names = {"-rt", "--responseTopic"}, description = "The topic name for the publish message`s response message", order = 1)
    @Nullable
    private String responseTopic;

    @CommandLine.Option(names = {"-cd", "--correlationData"}, converter = ByteBufferConverter.class, description = "The correlation data of the publish message", order = 1)
    @Nullable
    private ByteBuffer correlationData;


    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the publish message", order = 1)
    @Nullable
    private Mqtt5UserProperty[] userProperties;

    @Override
    public void run() {

        LoggingContext.put("identifier", "PUBLISH");

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        setDefaultOptions();
        sslConfig = buildSslConfig();

        logUnusedOptions();

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.publish(this);
        }
        catch (final Exception ex) {
            if (ex instanceof ConnectionFailedException) {
                LoggingContext.put("identifier", "CONNECT ERROR:");
            }
            else {
                LoggingContext.put("identifier", "SUBSCRIBE ERROR:");
            }
            if (isVerbose()) {
                Logger.trace(ex);
            }
            else if (isDebug()) {
                Logger.debug(ex.getMessage());
            }
            Logger.error(MqttUtils.getRootCause(ex).getMessage());
        }

    }

    public void logUnusedOptions() {

        super.logUnusedOptions();

        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (messageExpiryInterval != null) {
                Logger.warn("Publish message expiry was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (payloadFormatIndicator != null) {
                Logger.warn("Publish payload format indicator was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (contentType != null) {
                Logger.warn("Publish content type was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (responseTopic != null) {
                Logger.warn("Publish response topic was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (correlationData != null) {
                Logger.warn("Publish correlation data was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (userProperties != null) {
                Logger.warn("Publish user properties were set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }

    }

    @Override
    public String toString() {
        return "Publish:: {" +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", retain=" + retain +
                ", messageExpiryInterval=" + messageExpiryInterval +
                ", payloadFormatIndicator=" + payloadFormatIndicator +
                ", contentType=" + contentType +
                ", responseTopic=" + responseTopic +
                ", correlationData=" + correlationData +
                ", userProperties=" + getUserProperties() +
                ", Connect:: {" + connectOptions() + "}" +
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

    @NotNull
    @Override
    public ByteBuffer getMessage() {
        return message;
    }

    public void setMessage(final ByteBuffer message) {
        this.message = message;
    }

    @Nullable
    @Override
    public Boolean getRetain() {
        return retain;
    }

    public void setRetain(final @Nullable Boolean retain) {
        this.retain = retain;
    }

    @Override
    @Nullable
    public Long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    public void setMessageExpiryInterval(@Nullable final Long messageExpiryInterval) {
        this.messageExpiryInterval = messageExpiryInterval;
    }

    @Nullable
    @Override
    public Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public void setPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    @Nullable
    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(@Nullable final String contentType) {
        this.contentType = contentType;
    }

    @Nullable
    @Override
    public String getResponseTopic() {
        return responseTopic;
    }

    public void setResponseTopic(@Nullable final String responseTopic) {
        this.responseTopic = responseTopic;
    }

    @Nullable
    @Override
    public ByteBuffer getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(@Nullable final ByteBuffer correlationData) {
        this.correlationData = correlationData;
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
