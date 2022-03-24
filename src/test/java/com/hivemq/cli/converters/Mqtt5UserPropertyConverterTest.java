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

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Mqtt5UserPropertyConverterTest {

    private @NotNull Mqtt5UserPropertyConverter userPropertiesConverter;

    @BeforeEach
    void setUp() {
        userPropertiesConverter = new Mqtt5UserPropertyConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test=test", " = ", "3251251=1252151"})
    void testSuccess_OnePair(final @NotNull String s) throws Exception {
        final String[] keyValuePair = s.split("=");
        final Mqtt5UserProperty expected = Mqtt5UserProperty.of(keyValuePair[0], keyValuePair[1]);

        assertEquals(expected, userPropertiesConverter.convert(s));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "=", " =", "test=", "==="})
    void testFailure_NoDelimeterFound(final @NotNull String s) {
        final Exception e = assertThrows(Exception.class, () -> userPropertiesConverter.convert(s));
        assertEquals(Mqtt5UserPropertyConverter.KEY_VALUE_DELIMETER_ERROR, e.getMessage());
    }
}