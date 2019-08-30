package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface Subscribe extends Context {

    String[] getTopics();

    void setTopics(final String[] topics);

    MqttQos[] getQos();

    void setQos(final MqttQos[] qos);

    File getReceivedMessagesFile();

    void setReceivedMessagesFile(@Nullable final File receivedMessagesFile);

    boolean isPrintToSTDOUT();

    void setPrintToSTDOUT(final boolean printToSTDOUT);

    boolean isBase64();

    void setBase64(final boolean base64);

    @Nullable Mqtt5UserProperties getSubscribeUserProperties();

    void setSubscribeUserProperties(@Nullable final Mqtt5UserProperties subscribeUserProperties);
}
