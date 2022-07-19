package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.*;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class  PublishOptions {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0",
            description = "Quality of service for the corresponding topic (default for all: 0)")
    private @NotNull MqttQos @NotNull [] qos;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull MessagePayloadOptions message;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--retain"}, negatable = true,
            description = "The message will be retained (default: false)")
    private @Nullable Boolean retain;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-e", "--messageExpiryInterval"}, converter = UnsignedIntConverter.class,
            description = "The lifetime of the publish message in seconds (default: no message expiry)")
    private @Nullable Long messageExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-pf", "--payloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class,
            description = "The payload format indicator of the publish message")
    private @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-ct", "--contentType"}, description = "A description of publish message's content",
            order = 1)
    private @Nullable String contentType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-rt", "--responseTopic"},
            description = "The topic name for the publish message`s response message")
    private @Nullable String responseTopic;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-cd", "--correlationData"}, converter = ByteBufferConverter.class,
            description = "The correlation data of the publish message")
    private @Nullable ByteBuffer correlationData;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property of the publish message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    public @NotNull MqttQos @NotNull [] getQos() {
        return qos;
    }

    public @NotNull ByteBuffer getMessage() {
        return message.getMessageBuffer();
    }

    public @Nullable Boolean getRetain() {
        return retain;
    }

    public @Nullable Long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    public @Nullable Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public @Nullable String getContentType() {
        return contentType;
    }

    public @Nullable String getResponseTopic() {
        return responseTopic;
    }

    public @Nullable ByteBuffer getCorrelationData() {
        return correlationData;
    }

    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void logUnusedOptions(final @NotNull MqttVersion mqttVersion) {

        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (messageExpiryInterval != null) {
                Logger.warn("Publish message expiry was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (payloadFormatIndicator != null) {
                Logger.warn("Publish payload format indicator was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (contentType != null) {
                Logger.warn("Publish content type was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (responseTopic != null) {
                Logger.warn("Publish response topic was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (correlationData != null) {
                Logger.warn("Publish correlation data was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (userProperties != null) {
                Logger.warn("Publish user properties were set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }

    public void arrangeQosToMatchTopics(){
        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
    }

    public @NotNull String toString() {
        return getClass().getSimpleName() + "{topics=" + Arrays.toString(topics) +
                ", qos=" +
                Arrays.toString(qos) + ", message=" +
                new String(message.getMessageBuffer().array(), StandardCharsets.UTF_8) +
                (retain != null ? (", retain=" + retain) : "") +
                (messageExpiryInterval != null ? (", messageExpiryInterval=" + messageExpiryInterval) : "") +
                (payloadFormatIndicator != null ? (", payloadFormatIndicator=" + payloadFormatIndicator) : "") +
                (contentType != null ? (", contentType=" + contentType) : "") +
                (responseTopic != null ? (", responseTopic=" + responseTopic) : "") + (correlationData != null ?
                (", correlationData=" + new String(correlationData.array(), StandardCharsets.UTF_8)) : "") +
                (userProperties != null ? (", userProperties=" + getUserProperties()) : "") + '}';
    }
}

