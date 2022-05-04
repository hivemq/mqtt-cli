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

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.options.MessagePayloadOptions;
import com.hivemq.cli.converters.*;
import com.hivemq.cli.mqtt.MqttClientExecutor;
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

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@CommandLine.Command(name = "pub", aliases = "publish", description = "Publish a message to a list of topics")
public class ContextPublishCommand extends ShellContextCommand implements Runnable, Publish {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via required
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via default value
    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0",
            description = "Quality of service for the corresponding topic (default for all: 0)")
    private @NotNull MqttQos @NotNull [] qos;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via required
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull MessagePayloadOptions message;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--retain"}, negatable = true, defaultValue = "false",
            description = "The message will be retained (default: false)")
    private boolean retain;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-e", "--messageExpiryInterval"}, converter = UnsignedIntConverter.class,
            description = "The lifetime of the publish message in seconds (default: no message expiry)")
    private @Nullable Long messageExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-pf", "--payloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class,
            description = "The payload format indicator of the publish message")
    private @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-ct", "--contentType"}, description = "A description of publish message's content")
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

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextPublishCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ContextPublishCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @Override
    public void run() {
        logUnusedOptions();

        Logger.trace("Command {} ", this);

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.publish(Objects.requireNonNull(contextClient), this);
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "key=" + getKey() + ", topics=" + Arrays.toString(topics) + ", qos=" +
                Arrays.toString(qos) + ", message=" +
                new String(message.getMessageBuffer().array(), StandardCharsets.UTF_8) + ", retain=" + retain +
                (messageExpiryInterval != null ? (", messageExpiryInterval=" + messageExpiryInterval) : "") +
                (payloadFormatIndicator != null ? (", payloadFormatIndicator=" + payloadFormatIndicator) : "") +
                (contentType != null ? (", contentType=" + contentType) : "") +
                (responseTopic != null ? (", responseTopic=" + responseTopic) : "") + (correlationData != null ?
                (", correlationData=" + new String(correlationData.array(), StandardCharsets.UTF_8)) : "") +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") + '}';
    }

    @Override
    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    @Override
    public @NotNull MqttQos @NotNull [] getQos() {
        return qos;
    }

    @Override
    public @NotNull ByteBuffer getMessage() {
        return message.getMessageBuffer();
    }

    @Override
    public @Nullable Boolean getRetain() {
        return retain;
    }

    @Override
    public @Nullable Long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    @Override
    public @Nullable Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    @Override
    public @Nullable String getContentType() {
        return contentType;
    }

    @Override
    public @Nullable String getResponseTopic() {
        return responseTopic;
    }

    @Override
    public @Nullable ByteBuffer getCorrelationData() {
        return correlationData;
    }

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    private void logUnusedOptions() {
        if (Objects.requireNonNull(contextClient).getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
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
}
