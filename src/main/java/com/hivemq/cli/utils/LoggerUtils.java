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

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.options.DebugOptions;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Level;
import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LoggerUtils {

    public static void useDefaultLogging(final @Nullable Map<String, String> extendedProperties) {
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();
        final Path dir = defaultCLIProperties.getLogfilePath();
        final Level logLevel = defaultCLIProperties.getLogfileDebugLevel();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date date = new Date();
        final Path logfilePath = dir.resolve("mqtt_cli_" + dateFormat.format(date) + ".log");
        final String logfileFormatPattern = "{date: yyyy-MM-dd HH:mm:ss} | {pid} | {{level}|min-size=5} | {message}";
        final File dirFile = dir.toFile();

        //noinspection ResultOfMethodCallIgnored
        dirFile.mkdirs();

        // TinyLog configuration
        // File Writer (creates logfiles under .mqtt-cli/logs folder)
        final Map<String, String> configurationMap = new HashMap<String, String>() {{
            put("writer", "file");
            put("writer.format", logfileFormatPattern);
            put("writer.file", logfilePath.toString());
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
            if (logLevel.equals("debug") || logLevel.equals("trace")) {
                put("writer1.format", "{message}");
            } else {
                put("writer1.format", "{message-only}");
            }
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

    public static void logCommandError(
            final @NotNull String message,
            final @NotNull Exception exception,
            final @NotNull DebugOptions debugOptions) {
        final String exceptionMessage = Throwables.getRootCause(exception).getMessage();
        if (exceptionMessage != null) {
            Logger.error("{}. Reason: '{}'", message, exceptionMessage);
        } else {
            Logger.error("{}.", message);
        }
        if (!debugOptions.isDebug()) {
            Logger.error("Use '-d' or 'v' option to get more detailed information.");
        }
        if (debugOptions.isVerbose()) {
            Logger.error(exception);
        }
    }

    public static void logShellError(final @NotNull String message, final @NotNull Exception exception) {
        final String exceptionMessage = Throwables.getRootCause(exception).getMessage();
        if (exceptionMessage != null) {
            Logger.error(exception, "{}. Reason: '{}'", message, exceptionMessage);
        } else {
            Logger.error(exception, "{}. Use 'mqtt sh -l' to see more detailed information in the logfile.", message);
        }
    }
}
