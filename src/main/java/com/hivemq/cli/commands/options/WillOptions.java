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
package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.converters.PayloadFormatIndicatorConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WillOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wt", "--willTopic"}, description = "The topic of the will message")
    private @Nullable String willTopic;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wm", "--willMessage"},
                        converter = ByteBufferConverter.class,
                        description = "The payload of the will message")
    private @Nullable ByteBuffer willMessage;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wq", "--willQualityOfService"},
                        defaultValue = "0",
                        converter = MqttQosConverter.class,
                        description = "Quality of service level for the will message (default: 0)")
    private @Nullable MqttQos willQos;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wr", "--willRetain"},
                        defaultValue = "false",
                        description = "Will message as retained message (default: false)")
    private boolean willRetain;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-We", "--willMessageExpiryInterval"},
                        converter = UnsignedIntConverter.class,
                        description = "The lifetime of the will message in seconds (default: no message expiry)")
    private @Nullable Long willMessageExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wd", "--willDelayInterval"},
                        converter = UnsignedIntConverter.class,
                        description =
                                "The Server delays publishing the client's will message until the will delay has passed (default: " +
                                        Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL +
                                        ")")
    private @Nullable Long willDelayInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wpf", "--willPayloadFormatIndicator"},
                        converter = PayloadFormatIndicatorConverter.class,
                        description = "The payload format indicator of the will message")
    private @Nullable Mqtt5PayloadFormatIndicator willPayloadFormatIndicator;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wct", "--willContentType"},
                        description = "A description of the will message's content")
    private @Nullable String willContentType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wrt", "--willResponseTopic"},
                        description = "The topic name for the response message")
    private @Nullable String willResponseTopic;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wcd", "--willCorrelationData"},
                        converter = ByteBufferConverter.class,
                        description = "The correlation data of the will message")
    private @Nullable ByteBuffer willCorrelationData;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Wup", "--willUserProperty"},
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "A user property of the will message")
    private @Nullable Mqtt5UserProperty @Nullable [] willUserProperties;

    public @Nullable String getWillTopic() {
        return willTopic;
    }

    public @Nullable ByteBuffer getWillMessage() {
        return willMessage;
    }

    public @Nullable MqttQos getWillQos() {
        return willQos;
    }

    public @Nullable Boolean getWillRetain() {
        return willRetain;
    }

    public @Nullable Long getWillMessageExpiryInterval() {
        return willMessageExpiryInterval;
    }

    public @Nullable Long getWillDelayInterval() {
        return willDelayInterval;
    }

    public @Nullable Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator() {
        return willPayloadFormatIndicator;
    }

    public @Nullable String getWillContentType() {
        return willContentType;
    }

    public @Nullable String getWillResponseTopic() {
        return willResponseTopic;
    }

    public @Nullable ByteBuffer getWillCorrelationData() {
        return willCorrelationData;
    }

    public @Nullable Mqtt5UserProperties getWillUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(willUserProperties);
    }

    public void logUnusedOptions(final @NotNull MqttVersion mqttVersion) {
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (willMessageExpiryInterval != null) {
                Logger.warn("Will Message Expiry was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willPayloadFormatIndicator != null) {
                Logger.warn("Will Payload Format was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willDelayInterval != null) {
                Logger.warn("Will Delay Interval was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willContentType != null) {
                Logger.warn("Will Content Type was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willResponseTopic != null) {
                Logger.warn("Will Response Topic was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willCorrelationData != null) {
                Logger.warn("Will Correlation Data was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (willUserProperties != null) {
                Logger.warn("Will User Properties was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Override
    public @NotNull String toString() {
        return "WillOptions{" +
                "willTopic='" +
                willTopic +
                '\'' +
                ", willMessage=" +
                willMessage +
                ", willQos=" +
                willQos +
                ", willRetain=" +
                willRetain +
                ", willMessageExpiryInterval=" +
                willMessageExpiryInterval +
                ", willDelayInterval=" +
                willDelayInterval +
                ", willPayloadFormatIndicator=" +
                willPayloadFormatIndicator +
                ", willContentType='" +
                willContentType +
                '\'' +
                ", willResponseTopic='" +
                willResponseTopic +
                '\'' +
                ", willCorrelationData=" +
                willCorrelationData +
                ", willUserProperties=" +
                Arrays.toString(willUserProperties) +
                '}';
    }
}
