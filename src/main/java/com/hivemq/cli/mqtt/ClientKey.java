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

import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ClientKey {

    private final @Nullable String clientIdentifier;
    private final @NotNull String hostname;

    private ClientKey(final @Nullable String clientIdentifier, final @NotNull String hostname) {
        this.clientIdentifier = clientIdentifier;
        this.hostname = hostname;
    }

    public static ClientKey of(final @Nullable String clientIdentifier, final @NotNull String hostname) {
        return new ClientKey(clientIdentifier, hostname);
    }

    public static ClientKey of(final @NotNull MqttClient client) {
        return new ClientKey(client.getConfig().getClientIdentifier().map(Objects::toString).orElse(""),
                client.getConfig().getServerHost());
    }


    @Override
    public String toString() {
        return "Client{" + "clientIdentifier='" + clientIdentifier + '\'' + ", hostname='" + hostname + '\'' + '}';
    }
}
