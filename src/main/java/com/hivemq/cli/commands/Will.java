package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Will {

    @Nullable Long getWillMessageExpiryInterval();

    @Nullable String getWillTopic();

    @Nullable Boolean getWillRetain();

    @Nullable Long getWillDelayInterval();

    @Nullable Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator();

    @Nullable String getWillContentType();

    @Nullable String getWillResponseTopic();

    @Nullable ByteBuffer getWillCorrelationData();

    @Nullable Mqtt5UserProperties getWillUserProperties();

    @Nullable ByteBuffer getWillMessage();

    @Nullable MqttQos getWillQos();

}
