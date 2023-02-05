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
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.mqtt.clients.mqtt3.CliMqtt3Client;
import com.hivemq.cli.mqtt.clients.mqtt3.CliMqtt3ClientFactory;
import com.hivemq.cli.mqtt.clients.mqtt5.CliMqtt5Client;
import com.hivemq.cli.mqtt.clients.mqtt5.CliMqtt5ClientFactory;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConnectBuilder {

    final @NotNull ConnectOptions connectOptions;
    final @NotNull List<MqttClientDisconnectedListener> disconnectedListeners = new ArrayList<>();
    @Nullable SubscribeOptions subscribeOptions;

    public ConnectBuilder(final @NotNull ConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    public @NotNull ConnectBuilder subscribeOptions(final @NotNull SubscribeOptions subscribeOptions) {
        this.subscribeOptions = subscribeOptions;
        return this;
    }

    public @NotNull ConnectBuilder addDisconnectedListener(final @NotNull MqttClientDisconnectedListener disconnectedListener) {
        this.disconnectedListeners.add(disconnectedListener);
        return this;
    }

    public @NotNull CliMqttClient send() throws Exception {
        if (connectOptions.getVersion() == MqttVersion.MQTT_5_0) {
            final CliMqtt5Client
                    client = CliMqtt5ClientFactory.create(connectOptions, subscribeOptions, disconnectedListeners);
            client.connect(connectOptions);
            return client;
        } else {
            final CliMqtt3Client
                    client = CliMqtt3ClientFactory.create(connectOptions, subscribeOptions, disconnectedListeners);
            client.connect(connectOptions);
            return client;
        }
    }
}
