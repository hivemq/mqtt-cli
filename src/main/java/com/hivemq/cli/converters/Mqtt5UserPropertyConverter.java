/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.converters;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class Mqtt5UserPropertyConverter implements CommandLine.ITypeConverter<Mqtt5UserProperty> {

    static final @NotNull String KEY_VALUE_DELIMETER_ERROR = "a key value pair wasn't delimited by '='";

    @Override
    public @NotNull Mqtt5UserProperty convert(final @NotNull String s) throws Exception {
        final String[] splitKeyValue = s.split("=");

        if (splitKeyValue.length != 2) {
            throw new Exception(KEY_VALUE_DELIMETER_ERROR);
        }

        return Mqtt5UserProperty.of(splitKeyValue[0], splitKeyValue[1]);
    }
}
