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
package com.hivemq.cli.mqtt.clients.mqtt3;

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.WillOptions;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CliMqtt3ClientFactoryTest {

    private final ConnectOptions connectOptions = mock(ConnectOptions.class);
    private final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
    private final WillOptions willOptions = mock(WillOptions.class);

    @BeforeEach
    void setUp() {
        when(connectOptions.getHost()).thenReturn("test-host.com");
        when(connectOptions.getPort()).thenReturn(1883);
        when(connectOptions.getWillOptions()).thenReturn(willOptions);
    }

    @Test
    void create_whenClientIdIsMissing_thenClientIdIsAssigned() throws Exception {
        final CliMqtt3Client client = CliMqtt3ClientFactory.create(connectOptions, null, new ArrayList<>());

        assertFalse(client.getDelegate().getConfig().getClientIdentifier().isPresent());
        assertClient(client);
    }

    @Test
    void create_whenClientIdIsPresent_thenClientIdIsUsed() throws Exception {
        when(connectOptions.getIdentifier()).thenReturn("test-client");
        final CliMqtt3Client client = CliMqtt3ClientFactory.create(connectOptions, null, new ArrayList<>());

        assertThat(client.getDelegate().getConfig().getClientIdentifier()).contains(MqttClientIdentifier.of(
                "test-client"));
        assertClient(client);
    }

    @Test
    void create_whenWillIsPresent_thenWillIsUsed() throws Exception {
        when(willOptions.getWillTopic()).thenReturn("will-topic");
        when(willOptions.getWillMessage()).thenReturn(ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)));
        when(willOptions.getWillRetain()).thenReturn(true);
        when(willOptions.getWillQos()).thenReturn(MqttQos.AT_MOST_ONCE);

        final CliMqtt3Client client = CliMqtt3ClientFactory.create(connectOptions, null, new ArrayList<>());

        assertClient(client);

        final Mqtt3Client delegate = client.getDelegate();
        final MqttPublish publishDelegate = Mqtt3PublishView.delegate(MqttTopicImpl.of("will-topic"),
                ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)),
                MqttQos.AT_MOST_ONCE,
                true);
        final Mqtt3PublishView expectedWillPublish = Mqtt3PublishView.of(publishDelegate.asWill());
        assertFalse(delegate.getConfig().getClientIdentifier().isPresent());

        assertThat(delegate.getConfig().getWillPublish()).contains(expectedWillPublish);
    }

    @Test
    void create_whenWillMessageIsNotNull_thenThrowsIllegalArgumentException() {
        when(willOptions.getWillMessage()).thenReturn(ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)));

        assertThrows(IllegalArgumentException.class,
                () -> CliMqtt3ClientFactory.create(connectOptions, null, new ArrayList<>()),
                "option -wt is missing if a will message is configured - will options were: " + willOptions);
    }

    @Test
    void create_whenDisconnectedListenerIsPresent_thenDisconnectedListenerIsUsed() throws Exception {
        final ArrayList<MqttClientDisconnectedListener> disconnectedListeners = new ArrayList<>();
        disconnectedListeners.add(context -> {});
        disconnectedListeners.add(context -> {});
        disconnectedListeners.add(context -> {});

        final CliMqtt3Client client = CliMqtt3ClientFactory.create(connectOptions, null, disconnectedListeners);
        assertClient(client);

        assertEquals(3, client.getDelegate().getConfig().getDisconnectedListeners().size());
    }

    @Test
    void create_whenSubscribeOptionsArePresent_thenOptionsAreUsedForSubscribeCallback() throws Exception {
        final CliMqtt3Client client = CliMqtt3ClientFactory.create(connectOptions, subscribeOptions, new ArrayList<>());
        assertClient(client);
    }

    private static void assertClient(final @NotNull CliMqtt3Client client) {
        final Mqtt3Client delegate = client.getDelegate();
        final Mqtt3ClientConfig config = delegate.getConfig();

        assertEquals("test-host.com", config.getServerHost());
        assertEquals(1883, config.getServerPort());
        assertEquals(MqttVersion.MQTT_3_1_1, config.getMqttVersion());
        assertFalse(config.getSslConfig().isPresent());
    }
}
