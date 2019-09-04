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

import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.converters.*;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;

@CommandLine.Command(name = "pub",
        aliases = "publish",
        description = "Publish a message to a list of topics",
        abbreviateSynopsis = false)

public class PublishCommand extends AbstractConnectFlags implements MqttAction, Publish {

    private final MqttClientExecutor mqttClientExecutor;

    @Inject
    public PublishCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;

    }

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
    private Long publishExpiryInterval;

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

    @CommandLine.Option(names = {"-up", "--userProperties"}, converter = UserPropertiesConverter.class, description = "The user property of the publish message (usage: 'Key=Value', 'Key1=Value1|Key2=Value2)'", order = 1)
    @Nullable
    private Mqtt5UserProperties publishUserProperties;

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        setDefaultOptions();

        handleCommonOptions();

        logUnusedOptions();

        try {
            mqttClientExecutor.publish(this);
        }
        catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
        }

    }

    public void logUnusedOptions() {

        super.logUnusedOptions();

        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (publishExpiryInterval != null) {
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
            if (publishUserProperties != null) {
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
                ", messageExpiryInterval=" + publishExpiryInterval +
                ", payloadFormatIndicator=" + payloadFormatIndicator +
                ", contentType=" + contentType +
                ", responseTopic=" + responseTopic +
                ", correlationData=" + correlationData +
                ", userProperties=" + publishUserProperties +
                ", Connect:: {" + connectOptions() + "}" +
                '}';
    }


    @NotNull
    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    @NotNull
    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(final MqttQos[] qos) {
        this.qos = qos;
    }

    @NotNull
    public ByteBuffer getMessage() {
        return message;
    }

    public void setMessage(final ByteBuffer message) {
        this.message = message;
    }

    @Nullable
    public Boolean getRetain() {
        return retain;
    }

    public void setRetain(final @Nullable Boolean retain) {
        this.retain = retain;
    }

    public Long getPublishExpiryInterval() {
        return publishExpiryInterval;
    }

    public void setPublishExpiryInterval(@Nullable final Long publishExpiryInterval) {
        this.publishExpiryInterval = publishExpiryInterval;
    }

    @Nullable
    public Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public void setPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    @Nullable
    public String getContentType() {
        return contentType;
    }

    public void setContentType(@Nullable final String contentType) {
        this.contentType = contentType;
    }

    @Nullable
    public String getResponseTopic() {
        return responseTopic;
    }

    public void setResponseTopic(@Nullable final String responseTopic) {
        this.responseTopic = responseTopic;
    }

    @Nullable
    public ByteBuffer getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(@Nullable final ByteBuffer correlationData) {
        this.correlationData = correlationData;
    }

    @Nullable
    public Mqtt5UserProperties getPublishUserProperties() {
        return publishUserProperties;
    }

    public void setPublishUserProperties(@Nullable final Mqtt5UserProperties publishUserProperties) {
        this.publishUserProperties = publishUserProperties;
    }


}
