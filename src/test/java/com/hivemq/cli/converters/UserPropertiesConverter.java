package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class UserPropertiesConverter implements CommandLine.ITypeConverter<Mqtt5UserProperties> {

    static final String WRONG_INPUT_MESSAGE = "One key value pair has to be specified.";
    static final String UNEVEN_ARGUMENTS_MESSAGE = "An uneven amount of key value pairs was specified.";

    @Override
    public Mqtt5UserProperties convert(final @NotNull String s) throws Exception {

        String[] keyValuePairs = s.split(" ");

        // uneven key value pairs specified
        if (keyValuePairs.length % 2 != 0) {
            throw new Exception(UNEVEN_ARGUMENTS_MESSAGE);
        }

        Mqtt5UserPropertiesBuilder builder = Mqtt5UserProperties.builder();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            builder.add(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return builder.build();
    }
}
