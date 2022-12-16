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

package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.util.Arrays;

public class DisconnectOptions {

    @CommandLine.Option(names = {"-h", "--host"},
                        description = "The hostname of the message broker (default 'localhost')")
    private @Nullable String host;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--identifier"},
                        description = "The client identifier UTF-8 String (default randomly generated string)")
    private @Nullable String clientIdentifier;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-a", "--all"},
                        defaultValue = "false",
                        description = "Disconnect all connected clients")
    private boolean disconnectAll;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-e", "--sessionExpiryInterval"},
                        converter = UnsignedIntConverter.class,
                        description = "The session expiry of the disconnect (default: 0)")
    private @Nullable Long sessionExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--reason"}, description = "The reason of the disconnect")
    private @Nullable String reasonString;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"},
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "A user property of the disconnect message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    public boolean isDisconnectAll() {
        return disconnectAll;
    }

    public @Nullable Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public @Nullable String getReasonString() {
        return reasonString;
    }

    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public @Nullable String getHost() {
        return host;
    }

    public @Nullable String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setHost(final @NotNull String host) {
        this.host = host;
    }

    public void logUnusedDisconnectOptions(final @NotNull MqttVersion mqttVersion) {
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (sessionExpiryInterval != null) {
                Logger.warn("Session expiry interval set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }

            if (reasonString != null) {
                Logger.warn("Reason string was set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }

            if (userProperties != null) {
                Logger.warn("User properties were set but are unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Override
    public @NotNull String toString() {
        return "DisconnectOptions{" +
                "host='" +
                host +
                '\'' +
                ", identifier='" +
                clientIdentifier +
                '\'' +
                ", disconnectAll=" +
                disconnectAll +
                ", sessionExpiryInterval=" +
                sessionExpiryInterval +
                ", reasonString='" +
                reasonString +
                '\'' +
                ", userProperties=" +
                Arrays.toString(userProperties) +
                '}';
    }
}
