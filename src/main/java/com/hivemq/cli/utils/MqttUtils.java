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
package com.hivemq.cli.utils;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class MqttUtils {


    // Arrange the size of qos array to match the topics array size
    // The returned qos array will be filled with the default value represented by the first element in the given qos array
    // if the sizes dont match up. Else this method throws an IllegalArgument exception
    public static MqttQos[] arrangeQosToMatchTopics(final String[] topics, MqttQos[] qos) {
        if (topics.length != qos.length && qos.length == 1) {
            final MqttQos defaultQos = qos[0];
            qos = new MqttQos[topics.length];
            Arrays.fill(qos, defaultQos);
            return qos;
        } else if (topics.length == qos.length) {
            return qos;
        }
        throw new IllegalArgumentException("Topics do not match up to the QoS given. Topics Size {" + topics.length + "}, QoS Size {" + qos.length + "}");
    }

    public static @Nullable Mqtt5UserProperties convertToMqtt5UserProperties(final @Nullable Mqtt5UserProperty... userProperties) {
        if (userProperties == null) {
            return null;
        }
        else {
            return Mqtt5UserProperties.of(userProperties);
        }
    }
}
