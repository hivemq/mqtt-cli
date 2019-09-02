package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MqttVersionConverterTest {

    private MqttVersionConverter mqttVersionConverter;

    @BeforeEach
    void setUp() {
        mqttVersionConverter = new MqttVersionConverter();
    }

    @Test
    void convert_VERSION_5_SUCCESS() throws Exception {
        final MqttVersion expected = MqttVersion.MQTT_5_0;
        final MqttVersion actual = mqttVersionConverter.convert("5");

        assertEquals(expected, actual);
    }

    @Test
    void convert_VERSION_3_SUCCESS() throws Exception {
        final MqttVersion expected = MqttVersion.MQTT_3_1_1;
        final MqttVersion actual = mqttVersionConverter.convert("3");

        assertEquals(expected, actual);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"0", "1", "-1"})
    void convert_VERSION_UNSUPPORTED_FAILURE(final @NotNull String s) {
        Exception e = assertThrows(Exception.class, () -> mqttVersionConverter.convert(s));

        assertEquals(MqttVersionConverter.UNSUPPORTED_MQTT_VERSION, e.getMessage());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"A", "15.d", "ABC.3", "3,1", "3.1"})
    void convert_VERSION_BAD_NUMBER(final @NotNull String s) {
        Exception e = assertThrows(Exception.class, () -> mqttVersionConverter.convert(s));

        assertEquals(MqttVersionConverter.BAD_NUMBER_FORMAT, e.getMessage());
    }
}