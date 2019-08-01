package com.hivemq.cli.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnsignedShortConverterTest {

    private UnsignedShortConverter unsignedShortConverter;

    @BeforeEach
    void setUp() {
        unsignedShortConverter = new UnsignedShortConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "132.4", "0.5", "abc123", "123abc", "a.3", "3.b"})
    void testInvalidString(String s) {
        final Exception e = assertThrows(Exception.class, () -> unsignedShortConverter.convert(s));
        assertEquals(UnsignedShortConverter.WRONG_INPUT_MESSAGE, e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "65536", "-2147483648", "2147483647", "-1522155", "-5125125125"})
    void testNegativeNumber(String s) {
        final Exception e = assertThrows(java.lang.Exception.class, () -> unsignedShortConverter.convert(s));
        assertEquals(UnsignedShortConverter.WRONG_INPUT_MESSAGE, e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "65535", "5325", "23512"})
    void testSuccess(String s) throws Exception {
        long got = unsignedShortConverter.convert(s);
        long expected = Long.parseLong(s);
        assertEquals(expected, got);
    }
}