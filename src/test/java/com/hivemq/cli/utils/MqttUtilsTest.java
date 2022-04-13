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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MqttUtilsTest {

    @Test
    void testArrangeQosToMatchTopics_Success_One_QoS() {
        final String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE};

        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
        final MqttQos[] expected = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE};

        assertEquals(topics.length, qos.length);
        assertArrayEquals(expected, qos);
    }

    @Test
    void testArrangeQosToMatchTopics_Success_Full_QoS() {
        final String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.EXACTLY_ONCE};

        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
        final MqttQos[] expected = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.EXACTLY_ONCE};

        assertEquals(topics.length, qos.length);
        assertArrayEquals(expected, qos);
    }

    @Test
    void testArrangeQosToMatchTopics_Failure_Wrong_QoS_Amount() {
        final String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        final MqttQos[] qos = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE};

        assertThrows(IllegalArgumentException.class, () -> MqttUtils.arrangeQosToMatchTopics(topics, qos));
    }

    @Test
    void testArrangeQosToMatchTopics_Failure_Too_Many_QoS() {
        final String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        final MqttQos[] qos =
                {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE};

        assertThrows(IllegalArgumentException.class, () -> MqttUtils.arrangeQosToMatchTopics(topics, qos));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 8, 23, 54, 100})
    void testBuildRandomClientID_Success(final int source) {
        final String clientID = MqttUtils.buildRandomClientID(source);

        assertEquals(source, clientID.length());
    }

    @Test
    void testBuildRandomClientID_Failure_Negative() {
        assertThrows(IllegalArgumentException.class, () -> MqttUtils.buildRandomClientID(-1));
    }
}