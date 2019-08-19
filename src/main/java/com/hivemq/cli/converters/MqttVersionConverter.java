package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.MqttVersion;
import picocli.CommandLine;

public class MqttVersionConverter implements CommandLine.ITypeConverter<MqttVersion> {
    public static String UNSUPPORTED_MQTT_VERSION = "The specified MQTT Version is not supported.";

    @Override
    public MqttVersion convert(String value) throws Exception {
        int version = Integer.parseInt(value);
        switch (version) {
            case 3:
                return MqttVersion.MQTT_3_1_1;
            case 5:
                return MqttVersion.MQTT_5_0;
            default:
                throw new Exception(UNSUPPORTED_MQTT_VERSION);
        }
    }
}
