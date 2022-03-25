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

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Level;
import org.tinylog.configuration.Configuration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoggerUtils {

    public static void useDefaultLogging(final @Nullable Map<String, String> extendedProperties) {
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTTCLI).defaultCLIProperties();
        final String dir = defaultCLIProperties.getLogfilePath();
        final Level logLevel = defaultCLIProperties.getLogfileDebugLevel();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date date = new Date();
        final String logfilePath = dir + "mqtt_cli_" + dateFormat.format(date) + ".log";
        final String logfileFormatPattern = "{date: yyyy-MM-dd HH:mm:ss} | {pid} | {{level}|min-size=5} | {message}";
        final File dirFile = new File(dir);

        //noinspection ResultOfMethodCallIgnored
        dirFile.mkdirs();

        // TinyLog configuration
        // File Writer (creates logfiles under .mqtt-cli/logs folder)
        final Map<String, String> configurationMap = new HashMap<String, String>() {{
            put("writer", "file");
            put("writer.format", logfileFormatPattern);
            put("writer.file", logfilePath);
            put("writer.append", "true");
            put("writer.level", logLevel.name().toLowerCase());
        }};

        if (extendedProperties != null) {
            configurationMap.putAll(extendedProperties);
        }

        Configuration.replace(configurationMap);
    }

    public static void setupConsoleLogging(final boolean logToLogfile, final @NotNull String logLevel) {
        // TinyLog configuration
        final Map<String, String> configurationMap = new HashMap<String, String>() {{
            put("writer1", "console");
            put("writer1.format", "{message-only}");
            put("writer1.level", logLevel);
        }};

        if (logToLogfile) {
            LoggerUtils.useDefaultLogging(configurationMap);
        } else {
            Configuration.replace(configurationMap);
        }
    }

    public static void turnOffConsoleLogging(final boolean logToLogfile) {
        if (logToLogfile) {
            LoggerUtils.useDefaultLogging();
        } else {
            final Map<String, String> configurationMap = new HashMap<String, String>() {{
                put("writer.level", "off");
            }};
            Configuration.replace(configurationMap);
        }
    }

    public static void useDefaultLogging() {
        useDefaultLogging(null);
    }

    public static String getClientPrefix(final @NotNull MqttClientConfig config) {
        final Optional<MqttClientIdentifier> optId = config.getClientIdentifier();
        String id = optId.map(Objects::toString).orElse("UNKNOWN");
        if (id.isEmpty()) {
            id = "UNKNOWN";
        }
        return "Client '" + id + "@" + config.getServerHost() + "'";
    }
}
