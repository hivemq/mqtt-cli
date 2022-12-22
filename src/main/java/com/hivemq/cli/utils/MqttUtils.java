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

package com.hivemq.cli.utils;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MqttUtils {

    private static final @NotNull String CLIENT_ID_CHARSET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public enum IdentifierWarning {
        TOO_LONG,
        TOO_SHORT,
        CONTAINS_INVALID_CHAR
    }

    // Arrange the size of qos array to match the topics array size
    // The returned qos array will be filled with the default value represented by the first element in the given qos array
    // if the sizes do not match up. Else this method throws an IllegalArgument exception
    public static @NotNull MqttQos @NotNull [] arrangeQosToMatchTopics(
            final @NotNull String @NotNull [] topics, final @NotNull MqttQos @NotNull [] qos)
            throws IllegalArgumentException {
        if (topics.length != qos.length && qos.length == 1) {
            final MqttQos defaultQos = qos[0];
            final MqttQos[] newQos = new MqttQos[topics.length];
            Arrays.fill(newQos, defaultQos);
            return newQos;
        } else if (topics.length == qos.length) {
            return qos;
        }
        throw new IllegalArgumentException("Topics do not match up to the QoS given. Topics Size {" +
                topics.length +
                "}, QoS Size {" +
                qos.length +
                "}");
    }

    public static @Nullable Mqtt5UserProperties convertToMqtt5UserProperties(final @Nullable Mqtt5UserProperty @Nullable ... userProperties) {
        if (userProperties == null) {
            return null;
        } else {
            final List<Mqtt5UserProperty> nonNullUserProperties =
                    Arrays.stream(userProperties).filter(Objects::nonNull).collect(Collectors.toList());
            return Mqtt5UserProperties.of(nonNullUserProperties);
        }
    }

    // See http://docs.oasis-open.org/mqtt/mqtt/v5.0/cs02/mqtt-v5.0-cs02.html#_Toc514345331
    public static @NotNull String buildRandomClientID(final int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length of random client id has to be positive");
        }
        final SecureRandom rnd = new SecureRandom();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char rndChar = CLIENT_ID_CHARSET.charAt(rnd.nextInt(CLIENT_ID_CHARSET.length()));
            sb.append(rndChar);
        }
        return sb.toString();
    }

    public static List<IdentifierWarning> getIdentifierWarnings(final @NotNull String identifier) {
        final List<IdentifierWarning> identifierWarnings = new ArrayList<>();

        if (identifier.length() > 23) {
            identifierWarnings.add(IdentifierWarning.TOO_LONG);
        } else if (identifier.length() < 1) {
            identifierWarnings.add(IdentifierWarning.TOO_SHORT);
        }

        final boolean containsInvalidChar =
                identifier.chars().anyMatch((currChar) -> CLIENT_ID_CHARSET.indexOf((char) currChar) == -1);

        if (containsInvalidChar) {
            identifierWarnings.add(IdentifierWarning.CONTAINS_INVALID_CHAR);
        }

        return identifierWarnings;
    }

    public static char @NotNull [] getInvalidIdChars(final @NotNull String identifier) {
        return identifier.chars()
                .filter((currChar) -> CLIENT_ID_CHARSET.indexOf((char) currChar) == -1)
                .distinct()
                .mapToObj(c -> Character.toString((char) c))
                .collect(Collectors.joining())
                .toCharArray();
    }
}
