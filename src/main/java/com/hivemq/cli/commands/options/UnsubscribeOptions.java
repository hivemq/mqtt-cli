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
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnsubscribeOptions {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"},
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "A user property for the unsubscribe message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    public UnsubscribeOptions() {
    }

    public UnsubscribeOptions(
            final @NotNull String @NotNull [] topics, final @Nullable Mqtt5UserProperty @Nullable [] userProperties) {
        this.topics = topics;
        this.userProperties = userProperties;
    }

    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    public @NotNull Mqtt5UserProperties getUserProperties() {
        if (userProperties != null && userProperties.length > 0) {
            final List<Mqtt5UserProperty> nonNullProperties =
                    Arrays.stream(userProperties).filter(Objects::nonNull).collect(Collectors.toList());
            return Mqtt5UserProperties.of(nonNullProperties);
        } else {
            return Mqtt5UserProperties.of();
        }
    }

    public static @NotNull UnsubscribeOptions of(final @NotNull SubscribeOptions subscribeOptions) {
        return new UnsubscribeOptions(subscribeOptions.getTopics(), subscribeOptions.getUserPropertiesRaw());
    }

    public void logUnusedUnsubscribeOptions(final @NotNull MqttVersion mqttVersion) {
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Unsubscribe user properties were set but are unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }
}
