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

package com.hivemq.cli.utils;

import com.hivemq.extension.sdk.api.packets.general.MqttVersion;
import org.jetbrains.annotations.NotNull;

public class MqttVersionConverter {

    public static @NotNull MqttVersion toExtensionSdkVersion(final char version) {
        if (version == '3') {
            return MqttVersion.V_3_1_1;
        } else if (version == '5') {
            return MqttVersion.V_5;
        }
        throw new IllegalArgumentException("version " + version + " can not be converted to MqttVersion object.");
    }

    public static @NotNull com.hivemq.client.mqtt.MqttVersion toClientVersion(final char version) {
        if (version == '3') {
            return com.hivemq.client.mqtt.MqttVersion.MQTT_3_1_1;
        } else if (version == '5') {
            return com.hivemq.client.mqtt.MqttVersion.MQTT_5_0;
        }
        throw new IllegalArgumentException("version " + version + " can not be converted to MqttVersion object.");
    }
}
