package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.text.ParseException;

public class MqttVersionConverter implements CommandLine.ITypeConverter<MqttVersion> {
    public static String UNSUPPORTED_MQTT_VERSION = "The specified MQTT Version is not supported.";
    public static String BAD_NUMBER_FORMAT = "The given number can't be parsed to a valid MQTT Version";

    @Override
    public MqttVersion convert(final @NotNull String value) throws Exception {
        int version;
        try {
            version = Integer.parseInt(value);
        } catch (NumberFormatException throwable) {
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
