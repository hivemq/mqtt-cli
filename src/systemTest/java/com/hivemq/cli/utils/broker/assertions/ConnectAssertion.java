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

package com.hivemq.cli.utils.broker.assertions;

import com.google.common.collect.ImmutableList;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.connect.WillPublishPacket;
import com.hivemq.extension.sdk.api.packets.general.MqttVersion;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectAssertion {

    private @NotNull MqttVersion mqttVersion = MqttVersion.V_5;
    private @NotNull String clientId = "cliTest";
    private boolean cleanStart = true;
    private long sessionExpiryInterval = 0;
    private int keepAlive = 60;
    private int receiveMaximum = 65535;
    private long maximumPacketSize = 268435460;
    private int topicAliasMaximum = 0;
    private boolean requestProblemInformation = true;
    private boolean requestResponseInformation = false;

    private @Nullable String userName = null;
    private @Nullable ByteBuffer password = null;

    private @Nullable WillPublishPacket willPublish = null;
    private @Nullable UserProperties userProperties =
            UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder().build());

    private ConnectAssertion() {
    }

    public static void assertConnectPacket(
            final @NotNull ConnectPacket connectPacket,
            final @NotNull Consumer<ConnectAssertion> connectAssertionConsumer) {
        final ConnectAssertion connectAssertion = new ConnectAssertion();
        connectAssertionConsumer.accept(connectAssertion);
        assertEquals(connectAssertion.mqttVersion, connectPacket.getMqttVersion());
        assertEquals(connectAssertion.clientId, connectPacket.getClientId());
        assertEquals(connectAssertion.cleanStart, connectPacket.getCleanStart());
        assertEquals(connectAssertion.sessionExpiryInterval, connectPacket.getSessionExpiryInterval());
        assertEquals(connectAssertion.keepAlive, connectPacket.getKeepAlive());
        assertEquals(connectAssertion.receiveMaximum, connectPacket.getReceiveMaximum());
        assertEquals(connectAssertion.maximumPacketSize, connectPacket.getMaximumPacketSize());
        assertEquals(connectAssertion.topicAliasMaximum, connectPacket.getTopicAliasMaximum());
        assertEquals(connectAssertion.requestProblemInformation, connectPacket.getRequestProblemInformation());
        assertEquals(connectAssertion.requestResponseInformation, connectPacket.getRequestResponseInformation());

        assertEquals(Optional.ofNullable(connectAssertion.userName), connectPacket.getUserName());
        assertEquals(Optional.ofNullable(connectAssertion.password), connectPacket.getPassword());

        assertEquals(connectAssertion.userProperties, connectPacket.getUserProperties());
        if (connectAssertion.willPublish != null) {
            assertTrue(connectPacket.getWillPublish().isPresent());
            final WillPublishPacket expectedWill = connectAssertion.willPublish;
            final WillPublishPacket actualWill = connectPacket.getWillPublish().get();
            assertEquals(expectedWill.getPayload(), actualWill.getPayload());
            assertEquals(expectedWill.getTopic(), actualWill.getTopic());
            assertEquals(expectedWill.getWillDelay(), actualWill.getWillDelay());
            assertEquals(expectedWill.getUserProperties(), actualWill.getUserProperties());
            assertEquals(expectedWill.getContentType(), actualWill.getContentType());
            assertEquals(expectedWill.getCorrelationData(), actualWill.getCorrelationData());
            assertEquals(expectedWill.getMessageExpiryInterval(), actualWill.getMessageExpiryInterval());
            assertEquals(expectedWill.getQos(), actualWill.getQos());
            assertEquals(expectedWill.getResponseTopic(), actualWill.getResponseTopic());
            assertEquals(expectedWill.getRetain(), actualWill.getRetain());
            assertEquals(expectedWill.getSubscriptionIdentifiers(), actualWill.getSubscriptionIdentifiers());
        } else {
            assertFalse(connectPacket.getWillPublish().isPresent());
        }
    }

    public void setMqttVersion(final @NotNull MqttVersion mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public void setClientId(final @NotNull String clientId) {
        this.clientId = clientId;
    }

    public void setCleanStart(final boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public void setKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setReceiveMaximum(final int receiveMaximum) {
        this.receiveMaximum = receiveMaximum;
    }

    public void setMaximumPacketSize(final long maximumPacketSize) {
        this.maximumPacketSize = maximumPacketSize;
    }

    public void setTopicAliasMaximum(final int topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
    }

    public void setRequestProblemInformation(final boolean requestProblemInformation) {
        this.requestProblemInformation = requestProblemInformation;
    }

    public void setRequestResponseInformation(final boolean requestResponseInformation) {
        this.requestResponseInformation = requestResponseInformation;
    }

    public void setUserName(final @Nullable String userName) {
        this.userName = userName;
    }

    public void setPassword(final @Nullable ByteBuffer password) {
        this.password = password;
    }

    public void setWillPublish(final @Nullable WillPublishPacket willPublish) {
        this.willPublish = willPublish;
    }

    public void setUserProperties(final @Nullable UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
