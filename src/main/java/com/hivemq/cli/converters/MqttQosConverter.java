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

import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class MqttQosConverter implements CommandLine.ITypeConverter<MqttQos> {

    static final @NotNull String WRONG_INPUT_MESSAGE =
            "Value must be 0 (AT_MOST_ONCE), 1 (AT_LEAST_ONCE) or 2 (EXACTLY_ONCE)";

    @Override
    public @NotNull MqttQos convert(final @NotNull String s) throws Exception {
        switch (s.toLowerCase()) {
            case "0":
            case "at_most_once":
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
