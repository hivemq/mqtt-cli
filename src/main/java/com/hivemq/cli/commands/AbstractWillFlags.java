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
package com.hivemq.cli.commands;

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
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.util.Arrays;

@CommandLine.Command
public abstract class AbstractWillFlags extends MqttCommand implements Will {

    @CommandLine.Option(names = {"-Wt", "--willTopic"}, description = "The topic of the will message", order = 3)
    @Nullable
    private String willTopic;

    @CommandLine.Option(names = {"-Wm", "--willMessage"}, converter = ByteBufferConverter.class, description = "The payload of the will message", order = 3)
    @Nullable
    private ByteBuffer willMessage;

    @CommandLine.Option(names = {"-Wq", "--willQualityOfService"}, defaultValue = "0", converter = MqttQosConverter.class, description = "Quality of service level for the will message (default: 0)", order = 3)
    @Nullable
    private MqttQos willQos;

    @CommandLine.Option(names = {"-Wr", "--willRetain"}, negatable = true, description = "Will message as retained message (default: false)", order = 3)
    @Nullable
    private Boolean willRetain;

    @CommandLine.Option(names = {"-We", "--willMessageExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the will message in seconds (default: no message expiry)", order = 3)
    @Nullable
    private Long willMessageExpiryInterval;

    @CommandLine.Option(names = {"-Wd", "--willDelayInterval"}, converter = UnsignedIntConverter.class, description = "The Server delays publishing the client's will message until the will delay has passed (default: " + Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL + ")", order = 3)
    @Nullable
    private Long willDelayInterval;

    @CommandLine.Option(names = {"-Wpf", "--willPayloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class, description = "The payload format indicator of the will message", order = 3)
    @Nullable
    private Mqtt5PayloadFormatIndicator willPayloadFormatIndicator;

    @CommandLine.Option(names = {"-Wct", "--willContentType"}, description = "A description of the will message's content", order = 3)
    @Nullable
    private String willContentType;

    @CommandLine.Option(names = {"-Wrt", "--willResponseTopic"}, description = "The topic name for the response message", order = 3)
    @Nullable
    private String willResponseTopic;

    @CommandLine.Option(names = {"-Wcd", "--willCorrelationData"}, converter = ByteBufferConverter.class, description = "The correlation data of the will message", order = 3)
    @Nullable
    private ByteBuffer willCorrelationData;

    @CommandLine.Option(names = {"-Wup", "--willUserProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the will message", order = 3)
    @Nullable
    private Mqtt5UserProperty[] willUserProperties;


    String getWillOptions() {
        if (willTopic == null) {
            return "";
        }
        return  ", willTopic=" + willTopic +
                (willQos != null ? (", willQos=" + willQos) : "") +
                (willMessage != null ? (", willMessage=" + willMessage) : "") +
                (willRetain != null ? (", willRetain=" + willRetain) : "") +
                (willMessageExpiryInterval != null ? (", willMessageExpiryInterval=" + willMessageExpiryInterval) : "") +
                (willDelayInterval != null ? (", willDelayInterval=" + willDelayInterval) : "") +
                (willPayloadFormatIndicator != null ? (", willPayloadFormatIndicator=" + willPayloadFormatIndicator) : "") +
                (willContentType != null ? (", willContentType=" + willContentType) : "") +
                (willResponseTopic != null ? (", willResponseTopic=" + willResponseTopic) : "") +
                (willCorrelationData != null ? (", willCorrelationData=" + willCorrelationData) : "") +
                (willUserProperties != null ? (", willUserProperties=" + Arrays.toString(willUserProperties)) : "");
    }

    public void logUnusedOptions() {
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
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

    @Nullable
    @Override
    public String getWillTopic() {
        return willTopic;
    }

    @Nullable
    @Override
    public ByteBuffer getWillMessage() {
        return willMessage;
    }

    @Nullable
    @Override
    public MqttQos getWillQos() {
        return willQos;
    }

    @Nullable
    @Override
    public Boolean getWillRetain() {
        return willRetain;
    }

    @Nullable
    @Override
    public Long getWillMessageExpiryInterval() {
        return willMessageExpiryInterval;
    }

    @Nullable
    @Override
    public Long getWillDelayInterval() {
        return willDelayInterval;
    }

    @Nullable
    @Override
    public Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator() {
        return willPayloadFormatIndicator;
    }

    @Nullable
    @Override
    public String getWillContentType() {
        return willContentType;
    }

    @Nullable
    @Override
    public String getWillResponseTopic() {
        return willResponseTopic;
    }

    @Nullable
    @Override
    public ByteBuffer getWillCorrelationData() {
        return willCorrelationData;
    }

    @Nullable
    @Override
    public Mqtt5UserProperties getWillUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(willUserProperties);
    }

}
