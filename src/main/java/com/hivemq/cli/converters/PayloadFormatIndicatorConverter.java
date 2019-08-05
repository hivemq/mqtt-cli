package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class PayloadFormatIndicatorConverter implements CommandLine.ITypeConverter<Mqtt5PayloadFormatIndicator> {
    static final String WRONG_INPUT_MESSAGE = "Value must be UTF_8 or empty";


    @Override
    public Mqtt5PayloadFormatIndicator convert(final @NotNull String s) throws Exception {
        switch (s.toLowerCase()) {
            case "utf8":
            case "utf_8":
                return Mqtt5PayloadFormatIndicator.UTF_8;
            case "":
                return Mqtt5PayloadFormatIndicator.UNSPECIFIED;
            default:
                throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }

}
