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
package com.hivemq.cli.commands;

import com.hivemq.cli.converters.*;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;

@CommandLine.Command
public abstract class AbstractWillFlags extends MqttCommand {

    @CommandLine.Option(names = {"-Wt", "--willTopic"}, description = "The topic of the will message", order = 3)
    @Nullable
    private String willTopic;

    @CommandLine.Option(names = {"-Wm", "--willMessage"}, converter = ByteBufferConverter.class, description = "The payload of the will message", order = 3)
    @Nullable
    private ByteBuffer willMessage;

    @CommandLine.Option(names = {"-Wq", "--willQualityOfService"}, converter = MqttQosConverter.class, description = "Quality of service level for the will message (default: 0)", order = 3)
    @Nullable
    private MqttQos willQos;

    @CommandLine.Option(names = {"-Wr", "--willRetain"}, negatable = true, description = "Will message as retained message (default: false)", order = 3)
    @Nullable
    private Boolean willRetain;

    @CommandLine.Option(names = {"-We", "--willMessageExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the will message in seconds (default: no message expiry)", order = 3)
    @Nullable
    private Long willMessageExpiryInterval;

    @CommandLine.Option(names = {"-Wd", "--willDelayInterval"}, converter = UnsignedIntConverter.class, description = "The Server delays publishing the client's will message until the will delay has passed (default: 0)", order = 3)
    @Nullable
    private Long willDelayInterval;

    @CommandLine.Option(names = {"-Wp", "--willPayloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class, description = "The payload format indicator of the will message", order = 3)
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

    @CommandLine.Option(names = {"-Wup", "--willUserProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the will message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')", order = 3)
    @Nullable
    private Mqtt5UserProperties willUserProperties;


    String getWillOptions() {
        return "willTopic='" + willTopic + '\'' +
                ", willQos=" + willQos +
                ", willMessage='" + willMessage + '\'' +
                ", willRetain=" + willRetain +
                ", willMessageExpiryInterval=" + willMessageExpiryInterval +
                ", willDelayInterval=" + willDelayInterval +
                ", willPayloadFormatIndicator=" + willPayloadFormatIndicator +
                ", willContentType='" + willContentType + '\'' +
                ", willResponseTopic='" + willResponseTopic + '\'' +
                ", willCorrelationData=" + willCorrelationData +
                ", willUserProperties=" + willUserProperties;
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
    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(@Nullable final String willTopic) {
        this.willTopic = willTopic;
    }

    @Nullable
    public ByteBuffer getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(@Nullable final ByteBuffer willMessage) {
        this.willMessage = willMessage;
    }

    @Nullable
    public MqttQos getWillQos() {
        return willQos;
    }

    public void setWillQos(@Nullable final MqttQos willQos) {
        this.willQos = willQos;
    }

    @Nullable
    public Boolean getWillRetain() {
        return willRetain;
    }

    public void setWillRetain(final @Nullable Boolean willRetain) {
        this.willRetain = willRetain;
    }

    @Nullable
    public Long getWillMessageExpiryInterval() {
        return willMessageExpiryInterval;
    }

    public void setWillMessageExpiryInterval(@Nullable final Long willMessageExpiryInterval) {
        this.willMessageExpiryInterval = willMessageExpiryInterval;
    }

    @Nullable
    public Long getWillDelayInterval() {
        return willDelayInterval;
    }

    public void setWillDelayInterval(final long willDelayInterval) {
        this.willDelayInterval = willDelayInterval;
    }

    @Nullable
    public Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator() {
        return willPayloadFormatIndicator;
    }

    public void setWillPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator willPayloadFormatIndicator) {
        this.willPayloadFormatIndicator = willPayloadFormatIndicator;
    }

    @Nullable
    public String getWillContentType() {
        return willContentType;
    }

    public void setWillContentType(@Nullable final String willContentType) {
        this.willContentType = willContentType;
    }

    @Nullable
    public String getWillResponseTopic() {
        return willResponseTopic;
    }

    public void setWillResponseTopic(@Nullable final String willResponseTopic) {
        this.willResponseTopic = willResponseTopic;
    }

    @Nullable
    public ByteBuffer getWillCorrelationData() {
        return willCorrelationData;
    }

    public void setWillCorrelationData(@Nullable final ByteBuffer willCorrelationData) {
        this.willCorrelationData = willCorrelationData;
    }

    @Nullable
    public Mqtt5UserProperties getWillUserProperties() {
        return willUserProperties;
    }

    public void setWillUserProperties(@Nullable final Mqtt5UserProperties willUserProperties) {
        this.willUserProperties = willUserProperties;
    }
}
