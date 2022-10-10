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
import com.hivemq.extension.sdk.api.packets.disconnect.DisconnectPacket;
import com.hivemq.extension.sdk.api.packets.disconnect.DisconnectReasonCode;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisconnectAssertion {

    private @NotNull DisconnectReasonCode disconnectReasonCode = DisconnectReasonCode.NORMAL_DISCONNECTION;
    private @NotNull Optional<String> reasonString = Optional.empty();
    private @NotNull Optional<String> serverReference = Optional.empty();
    private @NotNull UserProperties userProperties = UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder().build());
    private @NotNull Optional<Long> sessionExpiryInterval = Optional.empty();

    private @Nullable String disconnectedClient = null;

    private DisconnectAssertion() {
    }

    public static void assertDisconnectPacket(final @NotNull DisconnectInformation disconnectInformation, final @NotNull Consumer<DisconnectAssertion> disconnectAssertionConsumer) {
        final DisconnectAssertion disconnectAssertion = new DisconnectAssertion();
        disconnectAssertionConsumer.accept(disconnectAssertion);

        final DisconnectPacket disconnectPacket = disconnectInformation.getDisconnectPacket();
        assertEquals(disconnectAssertion.disconnectReasonCode, disconnectPacket.getReasonCode());
        assertEquals(disconnectAssertion.reasonString, disconnectPacket.getReasonString());
        assertEquals(disconnectAssertion.serverReference, disconnectPacket.getServerReference());
        assertEquals(disconnectAssertion.userProperties, disconnectPacket.getUserProperties());
        assertEquals(disconnectAssertion.sessionExpiryInterval, disconnectPacket.getSessionExpiryInterval());

        if (disconnectAssertion.disconnectedClient != null) {
            assertEquals(disconnectAssertion.disconnectedClient, disconnectInformation.getClientId());
        }
    }

    public void setDisconnectReasonCode(final @NotNull DisconnectReasonCode disconnectReasonCode) {
        this.disconnectReasonCode = disconnectReasonCode;
    }

    public void setReasonString(final @NotNull String reasonString) {
        this.reasonString = Optional.of(reasonString);
    }

    public void setServerReference(final @NotNull String serverReference) {
        this.serverReference = Optional.of(serverReference);
    }

    public void setUserProperties(final @NotNull UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = Optional.of(sessionExpiryInterval);
    }

    public void setDisconnectedClient(final @NotNull String disconnectedClient) {
        this.disconnectedClient = disconnectedClient;
    }
}
