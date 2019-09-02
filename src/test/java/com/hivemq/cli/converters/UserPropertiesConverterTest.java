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
package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPropertiesConverterTest {

    private UserPropertiesConverter userPropertiesConverter;

    @BeforeEach
    void setUp() {
        userPropertiesConverter = new UserPropertiesConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test=test", " = ", "3251251=1252151"})
    void testSuccess_OnePair(final @NotNull String s) throws Exception {
        String[] keyValuePair = s.split("=");
        Mqtt5UserProperties expected = Mqtt5UserProperties.builder()
                .add(keyValuePair[0], keyValuePair[1])
                .build();

        assertEquals(expected, userPropertiesConverter.convert(s));
    }

    @Test
    void testSuccess_TwoPairs() throws Exception {
        String toConvert = "key1=value1|key2=value2";
        Mqtt5UserProperties expected = Mqtt5UserProperties.builder()
                .add("key1", "value1")
                .add("key2", "value2")
                .build();
        assertEquals(expected, userPropertiesConverter.convert(toConvert));
    }

    @Test
    void testSuccess_TwoPairs_SameKey() throws Exception {
        String toConvert = "key1=value1|key1=value2";
        Mqtt5UserProperties expected = Mqtt5UserProperties.builder()
                .add("key1", "value1")
                .add("key1", "value2")
                .build();
        assertEquals(expected, userPropertiesConverter.convert(toConvert));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "=", " =", "=|", "|=", " | =", "| |"})
    void testFailure_NoDelimeterFound(String s) {
        Exception e = assertThrows(Exception.class, () -> userPropertiesConverter.convert(s));
        assertEquals(UserPropertiesConverter.KEY_VALUE_DELIMETER_ERROR, e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"|", "||"})
    void testFailure_NoPairFound(String s) {
        Exception e = assertThrows(Exception.class, () -> userPropertiesConverter.convert(s));
        assertEquals(UserPropertiesConverter.NO_PAIR_FOUND, e.getMessage());
    }
}