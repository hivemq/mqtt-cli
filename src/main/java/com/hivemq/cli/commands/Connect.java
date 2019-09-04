package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Connect extends Context, Will, ConnectRestrictions {

    @NotNull String getHost();

    int getPort();

    @NotNull String getIdentifier();

    @NotNull MqttVersion getVersion();

    @Nullable String getUser();

    @Nullable ByteBuffer getPassword();

    @Nullable Integer getKeepAlive();

    @Nullable Boolean getCleanStart();

    @Nullable MqttClientSslConfig getSslConfig();

    @Nullable Long getConnectSessionExpiryInterval();

    @Nullable Mqtt5UserProperties getConnectUserProperties();


}
