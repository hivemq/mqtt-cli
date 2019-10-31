/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli;

import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttVersion;

import java.io.File;

public final class DefaultProperties {

    public static final String PROPERTIES_FILE_PATH = getPropertiesPath();

    public static final MqttVersion MQTT_VERSION = MqttVersion.MQTT_5_0;

    public static final String MQTT_VERSION_STRING = "5";

    public static final String HOST = "localhost";

    public static final int PORT = 1883;

    public static final PropertiesUtils.DEBUG_LEVEL SHELL_DEBUG_LEVEL = PropertiesUtils.DEBUG_LEVEL.VERBOSE;

    public static final String LOGFILE_PATH = getLogfilePath();

    public static final String SUBSCRIBE_OUTPUT_FILE = null;

    public static final String CLIENT_ID_PREFIX = "mqttClient";

    public static final int CLIENT_ID_LENGTH = 8;

    public static String getPropertiesPath() {
        return System.getProperty("user.home") + File.separator + ".mqtt-cli" + File.separator + "config.properties";
    }

    public static String getLogfilePath() {
        return System.getProperty("user.home") + File.separator + ".mqtt-cli" + File.separator + "logs" + File.separator;
    }
}
