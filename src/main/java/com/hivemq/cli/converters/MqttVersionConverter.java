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

import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class MqttVersionConverter implements CommandLine.ITypeConverter<MqttVersion> {

    static final @NotNull String UNSUPPORTED_MQTT_VERSION = "The specified MQTT Version is not supported.";
    static final @NotNull String BAD_NUMBER_FORMAT = "The given number can't be parsed to a valid MQTT Version";

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
