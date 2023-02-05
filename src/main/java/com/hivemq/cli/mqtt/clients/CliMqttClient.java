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
package com.hivemq.cli.mqtt.clients;

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.listeners.LogDisconnectedListener;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public interface CliMqttClient {

    static @NotNull ConnectBuilder connectWith(final @NotNull ConnectOptions connectOptions) {
        return new ConnectBuilder(connectOptions)
                .addDisconnectedListener(LogDisconnectedListener.INSTANCE);
    }

    void publish(final @NotNull PublishOptions publishOptions);

    void subscribe(final @NotNull SubscribeOptions subscribeOptions);

    void unsubscribe(final @NotNull UnsubscribeOptions unsubscribeOptions);

    void disconnect(final @NotNull DisconnectOptions disconnectOptions);

    boolean isConnected();

    @NotNull String getClientIdentifier();

    @NotNull String getServerHost();

    @NotNull MqttVersion getMqttVersion();

    @NotNull LocalDateTime getConnectedTime();

    @NotNull MqttClientState getState();

    @NotNull String getSslProtocols();

    int getServerPort();

    @NotNull List<MqttTopicFilter> getSubscribedTopics();
}
