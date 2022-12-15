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
import com.hivemq.extension.sdk.api.packets.disconnect.DisconnectPacket;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisconnectAssertion {

    private @Nullable String reasonString = null;
    private @NotNull UserProperties userProperties =
            UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder().build());
    private @Nullable Long sessionExpiryInterval = null;

    private @Nullable String disconnectedClient = null;

    private DisconnectAssertion() {
    }

    public static void assertDisconnectPacket(
            final @NotNull DisconnectInformation disconnectInformation,
            final @NotNull Consumer<DisconnectAssertion> disconnectAssertionConsumer) {
        final DisconnectAssertion disconnectAssertion = new DisconnectAssertion();
        disconnectAssertionConsumer.accept(disconnectAssertion);

        final DisconnectPacket disconnectPacket = disconnectInformation.getDisconnectPacket();
        assertEquals(Optional.ofNullable(disconnectAssertion.reasonString), disconnectPacket.getReasonString());
        assertEquals(disconnectAssertion.userProperties, disconnectPacket.getUserProperties());
        assertEquals(
                Optional.ofNullable(disconnectAssertion.sessionExpiryInterval),
                disconnectPacket.getSessionExpiryInterval());

        if (disconnectAssertion.disconnectedClient != null) {
            assertEquals(disconnectAssertion.disconnectedClient, disconnectInformation.getClientId());
        }
    }

    public void setReasonString(final @NotNull String reasonString) {
        this.reasonString = reasonString;
    }

    public void setUserProperties(final @NotNull UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public void setDisconnectedClient(final @NotNull String disconnectedClient) {
        this.disconnectedClient = disconnectedClient;
    }
}
