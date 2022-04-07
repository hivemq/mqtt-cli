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

package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.*;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractMqttClientExecutorTest {

    @NotNull Connect connect;

    @BeforeEach
    void setUp() {
        connect = mock(Connect.class);
        when(connect.getKey()).thenReturn("0");
        when(connect.getHost()).thenReturn("localhost");
        when(connect.getIdentifier()).thenReturn("client");
        when(connect.getReceiveMaximum()).thenReturn(null);
        when(connect.getSendMaximum()).thenReturn(null);
        when(connect.getMaximumPacketSize()).thenReturn(null);
        when(connect.getSendMaximumPacketSize()).thenReturn(null);
        when(connect.getTopicAliasMaximum()).thenReturn(null);
        when(connect.getSendTopicAliasMaximum()).thenReturn(null);
        when(connect.getRequestProblemInformation()).thenReturn(null);
        when(connect.getRequestResponseInformation()).thenReturn(null);
    }

    @Test
    void simpleAuth_whenNoAuthIsConfigured_thenNoAuthIsSet_Mqtt5() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_5_0);

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        final Mqtt5Client mqtt5Client = (Mqtt5Client) mqttClientExecutor.connect(connect);

        assertFalse(mqtt5Client.getConfig().getSimpleAuth().isPresent());
    }

    @Test
    void simpleAuth_whenNoAuthIsConfigured_thenNoAuthIsSet_Mqtt3() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        final Mqtt3Client mqtt5Client = (Mqtt3Client) mqttClientExecutor.connect(connect);

        assertFalse(mqtt5Client.getConfig().getSimpleAuth().isPresent());
    }

    @Test
    void simpleAuth_whenUsernameIsConfigured_setUsername_Mqtt5() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(connect.getUser()).thenReturn("Test");

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        mqttClientExecutor.connect(connect);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());

        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();

        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getUsername().isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().get().toString());
    }

    @Test
    void simpleAuth_whenUsernameIsConfigured_setUsername_Mqtt3() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(connect.getUser()).thenReturn("Test");

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        mqttClientExecutor.connect(connect);

        assertNotNull(mqttClientExecutor.getMqtt3ConnectMessage());

        final Optional<Mqtt3SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt3ConnectMessage().getSimpleAuth();

        assertTrue(simpleAuth.isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().toString());
    }

    @Test
    void simpleAuth_whenPasswordIsConfigured_setPassword_Mqtt5() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(connect.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        mqttClientExecutor.connect(connect);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());

        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();

        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getPassword().isPresent());
        assertEquals("Test", StandardCharsets.US_ASCII.decode(simpleAuth.get().getPassword().get()).toString());
    }

    @Test
    void simpleAuth_whenPasswordIsConfigured_setPassword_Mqtt3() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(connect.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        assertThrows(IllegalArgumentException.class, () -> mqttClientExecutor.connect(connect));
    }

    @Test
    void simpleAuth_whenUserNameAndPasswordIsConfigured_setUsernameAndPassword_Mqtt5() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(connect.getUser()).thenReturn("Test");
        when(connect.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        mqttClientExecutor.connect(connect);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());

        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();

        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getUsername().isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().get().toString());
        assertTrue(simpleAuth.get().getPassword().isPresent());
        assertEquals("Test", StandardCharsets.US_ASCII.decode(simpleAuth.get().getPassword().get()).toString());
    }

    @Test
    void simpleAuth_whenUserNameAndPasswordIsConfigured_setUsernameAndPassword_Mqtt3() {
        when(connect.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(connect.getUser()).thenReturn("Test");
        when(connect.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        final MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();
        mqttClientExecutor.connect(connect);

        assertNotNull(mqttClientExecutor.getMqtt3ConnectMessage());

        final Optional<Mqtt3SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt3ConnectMessage().getSimpleAuth();

        assertTrue(simpleAuth.isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().toString());
        assertTrue(simpleAuth.get().getPassword().isPresent());
        assertEquals("Test", StandardCharsets.US_ASCII.decode(simpleAuth.get().getPassword().get()).toString());
    }

    static class MqttClientExecutor extends AbstractMqttClientExecutor {

        private @Nullable Mqtt5Connect mqtt5ConnectMessage = null;
        private @Nullable Mqtt3Connect mqtt3ConnectMessage = null;

        @Override
        void mqtt5Connect(
                final @NotNull Mqtt5Client client,
                final @NotNull Mqtt5Connect connectMessage,
                final @NotNull Connect connect) {
            this.mqtt5ConnectMessage = connectMessage;
        }

        @Override
        void mqtt3Connect(
                final @NotNull Mqtt3Client client,
                final @NotNull Mqtt3Connect connectMessage,
                final @NotNull Connect connect) {
            this.mqtt3ConnectMessage = connectMessage;
        }

        @Override
        void mqtt5Subscribe(
                final @NotNull Mqtt5Client client,
                final @NotNull Subscribe subscribe,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt3Subscribe(
                final @NotNull Mqtt3Client client,
                final @NotNull Subscribe subscribe,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt5Publish(
                final @NotNull Mqtt5Client client,
                final @NotNull Publish publish,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt3Publish(
                final @NotNull Mqtt3Client client,
                final @NotNull Publish publish,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt5Unsubscribe(
                final @NotNull Mqtt5Client client, final @NotNull Unsubscribe unsubscribe) {}

        @Override
        void mqtt3Unsubscribe(
                final @NotNull Mqtt3Client client, final @NotNull Unsubscribe unsubscribe) {}

        @Override
        void mqtt5Disconnect(
                final @NotNull Mqtt5Client client, final @NotNull Disconnect disconnect) {}

        @Override
        void mqtt3Disconnect(
                final @NotNull Mqtt3Client client, final @NotNull Disconnect disconnect) {}

        public @Nullable Mqtt5Connect getMqtt5ConnectMessage() {
            return mqtt5ConnectMessage;
        }

        public @Nullable Mqtt3Connect getMqtt3ConnectMessage() {
            return mqtt3ConnectMessage;
        }
    }
}