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

package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MqttQosConverterTest {

    private @NotNull MqttQosConverter mqttQosConverter;

    @BeforeEach
    void setUp() {
        mqttQosConverter = new MqttQosConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "AT_MOST_ONCE", "at_most_once"})
    void testAtMostOnceSuccess(final @NotNull String s) throws Exception {
        final MqttQos expected = MqttQos.AT_MOST_ONCE;

        assertEquals(expected, mqttQosConverter.convert(s));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "AT_LEAST_ONCE", "at_least_once"})
    void testAtLeastOnceSuccess(final @NotNull String s) throws Exception {
        final MqttQos expected = MqttQos.AT_LEAST_ONCE;

        assertEquals(expected, mqttQosConverter.convert(s));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "EXACTLY_ONCE", "exactly_once"})
    void testExactlyOnceSuccess(final @NotNull String s) throws Exception {
        final MqttQos expected = MqttQos.EXACTLY_ONCE;

        assertEquals(expected, mqttQosConverter.convert(s));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "3", "test"})
    void testFailureInputs(final @NotNull String s) {
        final Exception e = assertThrows(Exception.class, () -> mqttQosConverter.convert(s));
        assertEquals(MqttQosConverter.WRONG_INPUT_MESSAGE, e.getMessage());
    }
}