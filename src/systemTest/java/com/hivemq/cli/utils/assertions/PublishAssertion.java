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
package com.hivemq.cli.utils.assertions;

import com.google.common.collect.ImmutableList;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.publish.PayloadFormatIndicator;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PublishAssertion {

    private @NotNull Qos qos = Qos.AT_MOST_ONCE;
    private boolean retain = false;
    private @NotNull String topic = "";
    private @NotNull Optional<ByteBuffer> payload = Optional.empty();
    private @NotNull Optional<ByteBuffer> correlationData = Optional.empty();
    private @NotNull Optional<String> contentType = Optional.empty();
    private @NotNull Optional<String> responseTopic = Optional.empty();
    private @NotNull Optional<Long> messageExpiryInterval = Optional.of(4294967296L);
    private @NotNull Optional<PayloadFormatIndicator> payloadFormatIndicator = Optional.empty();
    private @NotNull UserProperties userProperties = UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder().build());

    private PublishAssertion() {
    }

    public static void assertPublishPacket(final @NotNull PublishPacket publishPacket, final @NotNull Consumer<PublishAssertion> publishAssertionConsumer) {
        final PublishAssertion publishAssertion = new PublishAssertion();
        publishAssertionConsumer.accept(publishAssertion);

        assertEquals(publishAssertion.qos, publishPacket.getQos());
        assertEquals(publishAssertion.retain, publishPacket.getRetain());
        assertEquals(publishAssertion.topic, publishPacket.getTopic());
        assertEquals(publishAssertion.payload, publishPacket.getPayload());
        assertEquals(publishAssertion.correlationData, publishPacket.getCorrelationData());
        assertEquals(publishAssertion.contentType, publishPacket.getContentType());
        assertEquals(publishAssertion.responseTopic, publishPacket.getResponseTopic());
        assertEquals(publishAssertion.messageExpiryInterval, publishPacket.getMessageExpiryInterval());
        assertEquals(publishAssertion.payloadFormatIndicator, publishPacket.getPayloadFormatIndicator());
        assertEquals(publishAssertion.userProperties, publishPacket.getUserProperties());
    }

    public void setQos(final @NotNull Qos qos) {
        this.qos = qos;
    }

    public void setRetain(final boolean retain) {
        this.retain = retain;
    }

    public void setTopic(final @NotNull String topic) {
        this.topic = topic;
    }

    public void setPayload(final @NotNull ByteBuffer payload) {
        this.payload = Optional.of(payload);
    }

    public void setCorrelationData(final @NotNull ByteBuffer correlationData) {
        this.correlationData = Optional.of(correlationData);
    }

    public void setContentType(final String contentType) {
        this.contentType = Optional.of(contentType);
    }

    public void setResponseTopic(final @NotNull String responseTopic) {
        this.responseTopic = Optional.of(responseTopic);
    }

    public void setMessageExpiryInterval(final long messageExpiryInterval) {
        this.messageExpiryInterval = Optional.of(messageExpiryInterval);
    }

    public void setPayloadFormatIndicator(final @NotNull PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = Optional.of(payloadFormatIndicator);
    }

    public void setUserProperties(final @NotNull UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
