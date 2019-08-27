package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Publish extends Context {

    public String[] getTopics();

    public void setTopics(final String[] topics);

    public MqttQos[] getQos();

    public void setQos(final MqttQos[] qos);

    public ByteBuffer getMessage();

    public void setMessage(final ByteBuffer message);

    public boolean isRetain();

    public void setRetain(final boolean retain);

    public Long getMessageExpiryInterval();

    public void setMessageExpiryInterval(@Nullable final Long messageExpiryInterval);

    @Nullable
    public Mqtt5PayloadFormatIndicator getPayloadFormatIndicator();

    public void setPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator);

    @Nullable
    public String getContentType();

    public void setContentType(@Nullable final String contentType);

    @Nullable
    public String getResponseTopic();

    public void setResponseTopic(@Nullable final String responseTopic);

    @Nullable
    public ByteBuffer getCorrelationData();

    public void setCorrelationData(@Nullable final ByteBuffer correlationData);

    @Nullable
    public Mqtt5UserProperties getPublishUserProperties();

    public void setPublishUserProperties(@Nullable final Mqtt5UserProperties publishUserProperties);

}
