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

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayloadFormatIndicatorConverterTest {

    private @NotNull PayloadFormatIndicatorConverter payloadFormatIndicatorConverter;

    @BeforeEach
    void setUp() {
        payloadFormatIndicatorConverter = new PayloadFormatIndicatorConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"utf_8", "UTF8", "UTF_8"})
    void testSuccessUTF8(final @NotNull String s) throws Exception {
        final Mqtt5PayloadFormatIndicator expected = Mqtt5PayloadFormatIndicator.UTF_8;
        final Mqtt5PayloadFormatIndicator mqtt5PayloadFormatIndicator = payloadFormatIndicatorConverter.convert(s);

        assertEquals(expected, mqtt5PayloadFormatIndicator);
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    void testSuccessUnspecified(final @NotNull String s) throws Exception {
        final Mqtt5PayloadFormatIndicator expected = Mqtt5PayloadFormatIndicator.UNSPECIFIED;
        final Mqtt5PayloadFormatIndicator mqtt5PayloadFormatIndicator = payloadFormatIndicatorConverter.convert(s);

        assertEquals(expected, mqtt5PayloadFormatIndicator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"utf9", ".", " "})
    void testFailure(final @NotNull String s) {
        final Exception e = assertThrows(Exception.class, () -> payloadFormatIndicatorConverter.convert(s));
        assertEquals(PayloadFormatIndicatorConverter.WRONG_INPUT_MESSAGE, e.getMessage());
    }

}