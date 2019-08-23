package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class UserPropertiesConverter implements CommandLine.ITypeConverter<Mqtt5UserProperties> {

    static final String KEY_VALUE_DELIMETER_ERROR = "a key value pair wasn't delimited by '='";
    static final String NO_PAIR_FOUND = "No key value pair was given.";

    @Override
    public Mqtt5UserProperties convert(final @NotNull String s) throws Exception {

        String[] keyValuePairs = s.split("\\|");

        if (keyValuePairs.length == 0) {
            throw new Exception(NO_PAIR_FOUND);
        }

        Mqtt5UserPropertiesBuilder builder = Mqtt5UserProperties.builder();

        for (final String pair : keyValuePairs) {
            final String[] splittedKeyValue = pair.split("=");

            if (splittedKeyValue.length != 2) {
                throw new Exception(KEY_VALUE_DELIMETER_ERROR);
            }

            builder.add(splittedKeyValue[0], splittedKeyValue[1]);
        }
        return builder.build();
    }
}
