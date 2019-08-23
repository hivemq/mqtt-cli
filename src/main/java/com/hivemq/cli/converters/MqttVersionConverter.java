package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.text.ParseException;

public class MqttVersionConverter implements CommandLine.ITypeConverter<MqttVersion> {
    static final String UNSUPPORTED_MQTT_VERSION = "The specified MQTT Version is not supported.";
    static final String BAD_NUMBER_FORMAT = "The given number can't be parsed to a valid MQTT Version";

    @Override
    public @NotNull MqttVersion convert(final @NotNull String value) throws Exception {
        final int version;
        try {
            version = Integer.parseInt(value);
        } catch (final NumberFormatException throwable) {
            throw new IllegalArgumentException(BAD_NUMBER_FORMAT);
        }

        switch (version) {
            case 3:
                return MqttVersion.MQTT_3_1_1;
            case 5:
                return MqttVersion.MQTT_5_0;
            default:
                throw new IllegalArgumentException(UNSUPPORTED_MQTT_VERSION);
        }
    }
}
