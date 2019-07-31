package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayloadFormatIndicatorConverterTest {

    private PayloadFormatIndicatorConverter payloadFormatIndicatorConverter;

    @BeforeEach
    void setUp() {
        payloadFormatIndicatorConverter = new PayloadFormatIndicatorConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"utf_8", "UTF8", "UTF_8"})
    void testSuccessUTF8(final @NotNull String s) throws Exception {
        Mqtt5PayloadFormatIndicator expected = Mqtt5PayloadFormatIndicator.UTF_8;
        Mqtt5PayloadFormatIndicator mqtt5PayloadFormatIndicator = payloadFormatIndicatorConverter.convert(s);
        assertEquals(expected, mqtt5PayloadFormatIndicator);
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    void testSuccessUnspecified(final @NotNull String s) throws Exception {
        Mqtt5PayloadFormatIndicator expected = Mqtt5PayloadFormatIndicator.UNSPECIFIED;
        Mqtt5PayloadFormatIndicator mqtt5PayloadFormatIndicator = payloadFormatIndicatorConverter.convert(s);
        assertEquals(expected, mqtt5PayloadFormatIndicator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"utf9", ".", " "})
    void testFailure(final @NotNull String s) {
        Exception e = assertThrows(Exception.class, () -> payloadFormatIndicatorConverter.convert(s));
        assertEquals(PayloadFormatIndicatorConverter.WRONG_INPUT_MESSAGE, e.getMessage());
    }


}