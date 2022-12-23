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
import com.hivemq.extension.sdk.api.packets.general.MqttVersion;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestConnectAssertion {

    private @NotNull MqttVersion mqttVersion = MqttVersion.V_5;
    private final boolean cleanStart = true;
    private final long sessionExpiryInterval = 0;
    private final int keepAlive = 60;
    private final int receiveMaximum = 65535;
    private final long maximumPacketSize = 268435460;
    private final int topicAliasMaximum = 0;
    private final boolean requestProblemInformation = true;
    private final boolean requestResponseInformation = false;

    private @Nullable String userName = null;
    private @Nullable ByteBuffer password = null;

    private final @Nullable UserProperties userProperties =
            UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder().build());

    private TestConnectAssertion() {
    }

    public static void assertTestConnectPacket(
            final @NotNull ConnectPacket connectPacket,
            final @NotNull Consumer<TestConnectAssertion> connectAssertionConsumer) {
        final TestConnectAssertion connectAssertion = new TestConnectAssertion();
        connectAssertionConsumer.accept(connectAssertion);
        assertEquals(connectAssertion.mqttVersion, connectPacket.getMqttVersion());
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
        assertFalse(connectPacket.getWillPublish().isPresent());
    }

    public void setMqttVersion(final @NotNull MqttVersion mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public void setUserName(final @Nullable String userName) {
        this.userName = userName;
    }

    public void setPassword(final @Nullable ByteBuffer password) {
        this.password = password;
    }
}
