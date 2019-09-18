/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Connect extends Context, Will, ConnectRestrictions {

    @NotNull String getHost();

    int getPort();

    @NotNull String getIdentifier();

    @NotNull MqttVersion getVersion();

    @Nullable String getUser();

    @Nullable ByteBuffer getPassword();

    @Nullable Integer getKeepAlive();

    @Nullable Boolean getCleanStart();

    @Nullable MqttClientSslConfig getSslConfig();

    @Nullable Long getSessionExpiryInterval();

    @Nullable Mqtt5UserProperties getConnectUserProperties();


}
