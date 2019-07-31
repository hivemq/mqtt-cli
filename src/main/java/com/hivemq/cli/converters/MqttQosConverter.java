package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class MqttQosConverter implements CommandLine.ITypeConverter<MqttQos> {
    public static final String WRONG_INPUT_MESSAGE = "Value must be 0 (AT_MOST_ONCE), 1 (AT_LEAST_ONCE) or 2 (EXACTLY_ONCE)";


    @Override
    public MqttQos convert(final @NotNull String s) throws Exception {
        switch (s.toLowerCase()) {
            case "0":
            case "at_most_once" :
                return MqttQos.AT_MOST_ONCE;
            case "1":
            case "at_least_once":
                return MqttQos.AT_LEAST_ONCE;
            case "2":
            case "exactly_once":
                return MqttQos.EXACTLY_ONCE;
            default:
                throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }
}
