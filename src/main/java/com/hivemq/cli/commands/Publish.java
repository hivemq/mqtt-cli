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

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Publish extends Context {

    String[] getTopics();

    void setTopics(final String[] topics);

    MqttQos[] getQos();

    void setQos(final MqttQos[] qos);

    ByteBuffer getMessage();

    void setMessage(final ByteBuffer message);

    boolean isRetain();

    void setRetain(final boolean retain);

    Long getMessageExpiryInterval();

    void setMessageExpiryInterval(@Nullable final Long messageExpiryInterval);

    @Nullable
    Mqtt5PayloadFormatIndicator getPayloadFormatIndicator();

    void setPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator);

    @Nullable
    String getContentType();

    void setContentType(@Nullable final String contentType);

    @Nullable
    String getResponseTopic();

    void setResponseTopic(@Nullable final String responseTopic);

    @Nullable
    ByteBuffer getCorrelationData();

    void setCorrelationData(@Nullable final ByteBuffer correlationData);

    @Nullable
    Mqtt5UserProperties getPublishUserProperties();

    void setPublishUserProperties(@Nullable final Mqtt5UserProperties publishUserProperties);

}
