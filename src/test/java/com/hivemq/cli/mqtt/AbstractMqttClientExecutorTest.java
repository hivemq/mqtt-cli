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

import com.hivemq.cli.commands.options.*;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttSharedTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractMqttClientExecutorTest {

    private final @NotNull MqttClientExecutor mqttClientExecutor = new MqttClientExecutor();

    private @NotNull ConnectOptions connectOptions;
    private @NotNull ConnectRestrictionOptions connectRestrictionOptions;
    private @NotNull AuthenticationOptions authenticationOptions;
    private @NotNull WillOptions willOptions;

    @BeforeEach
    void setUp() {
        connectOptions = mock(ConnectOptions.class);
        connectRestrictionOptions = mock(ConnectRestrictionOptions.class);
        authenticationOptions = mock(AuthenticationOptions.class);
        willOptions = mock(WillOptions.class);
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(connectRestrictionOptions);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(connectOptions.getWillOptions()).thenReturn(willOptions);
        when(connectOptions.getHost()).thenReturn("localhost");
        when(connectOptions.getIdentifier()).thenReturn("client");
        when(connectRestrictionOptions.getReceiveMaximum()).thenReturn(null);
        when(connectRestrictionOptions.getSendMaximum()).thenReturn(null);
        when(connectRestrictionOptions.getMaximumPacketSize()).thenReturn(null);
        when(connectRestrictionOptions.getSendMaximumPacketSize()).thenReturn(null);
        when(connectRestrictionOptions.getTopicAliasMaximum()).thenReturn(null);
        when(connectRestrictionOptions.getSendTopicAliasMaximum()).thenReturn(null);
        when(connectRestrictionOptions.getRequestProblemInformation()).thenReturn(null);
        when(connectRestrictionOptions.getRequestResponseInformation()).thenReturn(null);
    }

    @Test
    void checkForSharedTopicDuplicate_noExisting_normalNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();

        final String newTopic = "a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertTrue(duplicateList.isEmpty());
    }

    @Test
    void checkForSharedTopicDuplicate_normalExisting_normalNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttTopicFilter.of("a"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertFalse(duplicateList.isEmpty());
        assertEquals(MqttTopicFilter.of("a"), duplicateList.get(0));
    }

    @Test
    void checkForSharedTopicDuplicate_normalExisting_sharedNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttTopicFilter.of("a"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "$share/group/a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertFalse(duplicateList.isEmpty());
        assertEquals(MqttTopicFilter.of("a"), duplicateList.get(0));
        //assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0).getValue());
    }

    @Test
    void checkForSharedTopicDuplicate_sharedExisting_normalNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttSharedTopicFilter.of("group", "a"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertFalse(duplicateList.isEmpty());
        assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0));
        //assertEquals(MqttTopicFilter.of("a"), duplicateList.get(0).getValue());
    }

    @Test
    void checkForSharedTopicDuplicate_sharedExisting_sharedNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttSharedTopicFilter.of("group", "a"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "$share/group/a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertFalse(duplicateList.isEmpty());
        assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0));
        //assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0).getValue());
    }

    @Test
    void checkForSharedTopicDuplicate_multipleDisjointSharedExisting_normalNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttSharedTopicFilter.of("group", "a"));
        existingFilter.add(MqttSharedTopicFilter.of("group", "b"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertEquals(1, duplicateList.size());
        assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0));
        //assertEquals(MqttTopicFilter.of("a"), duplicateList.get(0).getValue());
    }

    @Test
    void checkForSharedTopicDuplicate_multipleIntersectingSharedExisting_normalNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttSharedTopicFilter.of("group", "a"));
        existingFilter.add(MqttSharedTopicFilter.of("group", "+"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertEquals(2, duplicateList.size());
        assertTrue(duplicateList.stream().anyMatch(topicFilter -> {
            //assertNotNull(topicFilter.getValue());
            return topicFilter.equals(MqttSharedTopicFilter.of("group", "+"));
        }));
        assertTrue(duplicateList.stream().anyMatch(topicFilter -> {
            //assertNotNull(topicFilter.getValue());
            return topicFilter.equals(MqttSharedTopicFilter.of("group", "a"));
        }));
        //assertEquals(MqttTopicFilter.of("a"), duplicateList.get(0).getValue());
        //assertEquals(MqttTopicFilter.of("a"), duplicateList.get(1).getValue());
    }

    @Test
    void checkForSharedTopicDuplicate_multipleIntersectingNormalExisting_SharedNew() {
        final Set<MqttTopicFilter> existingFilter = new HashSet<>();
        existingFilter.add(MqttTopicFilter.of("a"));
        existingFilter.add(MqttTopicFilter.of("+"));
        existingFilter.add(MqttTopicFilter.of("b"));

        final String newTopic = "$share/group/a";
        final List<MqttTopicFilter> duplicateList =
                mqttClientExecutor.checkForSharedTopicDuplicate(existingFilter, newTopic);
        assertEquals(2, duplicateList.size());
        //assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(0).getValue());
        //assertEquals(MqttSharedTopicFilter.of("group", "a"), duplicateList.get(1).getValue());
        assertTrue(duplicateList.stream().anyMatch(topicFilter -> topicFilter.equals(MqttTopicFilter.of("a"))));
        assertTrue(duplicateList.stream().anyMatch(topicFilter -> topicFilter.equals(MqttTopicFilter.of("+"))));
    }

    @Test
    void simpleAuth_whenNoAuthIsConfigured_thenNoAuthIsSet_Mqtt5() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_5_0);

        final Mqtt5Client mqtt5Client = (Mqtt5Client) mqttClientExecutor.connect(connectOptions);
        assertFalse(mqtt5Client.getConfig().getSimpleAuth().isPresent());
    }

    @Test
    void simpleAuth_whenNoAuthIsConfigured_thenNoAuthIsSet_Mqtt3() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);

        final Mqtt3Client mqtt5Client = (Mqtt3Client) mqttClientExecutor.connect(connectOptions);
        assertFalse(mqtt5Client.getConfig().getSimpleAuth().isPresent());
    }

    @Test
    void simpleAuth_whenUsernameIsConfigured_setUsername_Mqtt5() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(authenticationOptions.getUser()).thenReturn("Test");

        mqttClientExecutor.connect(connectOptions);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());
        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();
        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getUsername().isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().get().toString());
    }

    @Test
    void simpleAuth_whenUsernameIsConfigured_setUsername_Mqtt3() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(authenticationOptions.getUser()).thenReturn("Test");

        mqttClientExecutor.connect(connectOptions);

        assertNotNull(mqttClientExecutor.getMqtt3ConnectMessage());
        final Optional<Mqtt3SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt3ConnectMessage().getSimpleAuth();
        assertTrue(simpleAuth.isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().toString());
    }

    @Test
    void simpleAuth_whenPasswordIsConfigured_setPassword_Mqtt5() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        mqttClientExecutor.connect(connectOptions);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());
        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();
        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getPassword().isPresent());
        assertEquals("Test", StandardCharsets.US_ASCII.decode(simpleAuth.get().getPassword().get()).toString());
    }

    @Test
    void simpleAuth_whenPasswordIsConfigured_setPassword_Mqtt3() {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> mqttClientExecutor.connect(connectOptions));
    }

    @Test
    void simpleAuth_whenUserNameAndPasswordIsConfigured_setUsernameAndPassword_Mqtt5() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_5_0);
        when(authenticationOptions.getUser()).thenReturn("Test");
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        mqttClientExecutor.connect(connectOptions);

        assertNotNull(mqttClientExecutor.getMqtt5ConnectMessage());
        final Optional<Mqtt5SimpleAuth> simpleAuth = mqttClientExecutor.getMqtt5ConnectMessage().getSimpleAuth();
        assertTrue(simpleAuth.isPresent());
        assertTrue(simpleAuth.get().getUsername().isPresent());
        assertEquals("Test", simpleAuth.get().getUsername().get().toString());
        assertTrue(simpleAuth.get().getPassword().isPresent());
        assertEquals("Test", StandardCharsets.US_ASCII.decode(simpleAuth.get().getPassword().get()).toString());
    }

    @Test
    void simpleAuth_whenUserNameAndPasswordIsConfigured_setUsernameAndPassword_Mqtt3() throws Exception {
        when(connectOptions.getVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(authenticationOptions.getUser()).thenReturn("Test");
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("Test".getBytes(StandardCharsets.UTF_8)));

        mqttClientExecutor.connect(connectOptions);

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
                final @NotNull Mqtt5Connect connectMessage) {
            this.mqtt5ConnectMessage = connectMessage;
        }

        @Override
        void mqtt3Connect(
                final @NotNull Mqtt3Client client,
                final @NotNull Mqtt3Connect connectMessage) {
            this.mqtt3ConnectMessage = connectMessage;
        }

        @Override
        void mqtt5Subscribe(
                final @NotNull Mqtt5Client client,
                final @NotNull SubscribeOptions subscribeOptions,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt3Subscribe(
                final @NotNull Mqtt3Client client,
                final @NotNull SubscribeOptions subscribeOptions,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt5Publish(
                final @NotNull Mqtt5Client client,
                final @NotNull PublishOptions publishOptions,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt3Publish(
                final @NotNull Mqtt3Client client,
                final @NotNull PublishOptions publishOptions,
                final @NotNull String topic,
                final @NotNull MqttQos qos) {}

        @Override
        void mqtt5Unsubscribe(
                final @NotNull Mqtt5Client client, final @NotNull UnsubscribeOptions unsubscribeOptions) {}

        @Override
        void mqtt3Unsubscribe(
                final @NotNull Mqtt3Client client, final @NotNull UnsubscribeOptions unsubscribeOptions) {}

        @Override
        void mqtt5Disconnect(
                final @NotNull Mqtt5Client client, final @NotNull DisconnectOptions disconnectOptions) {}

        @Override
        void mqtt3Disconnect(
                final @NotNull Mqtt3Client client, final @NotNull DisconnectOptions disconnectOptions) {}

        public @Nullable Mqtt5Connect getMqtt5ConnectMessage() {
            return mqtt5ConnectMessage;
        }

        public @Nullable Mqtt3Connect getMqtt3ConnectMessage() {
            return mqtt3ConnectMessage;
        }
    }
}