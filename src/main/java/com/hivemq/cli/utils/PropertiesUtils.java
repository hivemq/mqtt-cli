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
package com.hivemq.cli.utils;

import com.hivemq.cli.DefaultProperties;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Properties;

public class PropertiesUtils {

    // Default values

    public enum DEBUG_LEVEL {
        INFO, DEBUG, VERBOSE;
    }

    public static final String PROPERTIES_FILE_PATH = DefaultProperties.PROPERTIES_FILE_PATH;

    public static MqttVersion DEFAULT_MQTT_VERSION = DefaultProperties.MQTT_VERSION;

    public static String DEFAULT_MQTT_VERSION_STRING = DefaultProperties.MQTT_VERSION_STRING;

    public static String DEFAULT_HOST = DefaultProperties.HOST;

    public static int DEFAULT_PORT = DefaultProperties.PORT;

    public static DEBUG_LEVEL DEFAULT_SHELL_DEBUG_LEVEL = DefaultProperties.SHELL_DEBUG_LEVEL;

    public static String DEFAULT_CLIENT_PREFIX = DefaultProperties.CLIENT_PREFIX;

    public static String DEFAULT_LOGFILE_PATH = DefaultProperties.LOGFILE_PATH;

    public static String DEFAULT_SUBSCRIBE_OUTPUT_FILE = DefaultProperties.SUBSCRIBE_OUTPUT_FILE;

    public static Properties DEFAULT_PROPERTIES = getDefaultProperties();




    public static Properties getDefaultProperties() {
        final Properties properties = new Properties();

        properties.setProperty("mqtt.version", DEFAULT_MQTT_VERSION_STRING);
        properties.setProperty("mqtt.host", DEFAULT_HOST);
        properties.setProperty("mqtt.port", String.valueOf(DEFAULT_PORT));
        properties.setProperty("debug.level.shell", DEFAULT_SHELL_DEBUG_LEVEL.name());
        properties.setProperty("debug.logfile.path", DEFAULT_LOGFILE_PATH);
        properties.setProperty("client.prefix", DEFAULT_CLIENT_PREFIX);
        if (DEFAULT_SUBSCRIBE_OUTPUT_FILE != null) {
            properties.setProperty("client.subscribe.output", DEFAULT_SUBSCRIBE_OUTPUT_FILE);
        }

        return properties;
    }


    public static Properties createDefaultPropertiesFile(final @NotNull Properties properties, final @NotNull String propertiesPath) throws IOException {

        final File file = new File(propertiesPath);

        file.getParentFile().mkdirs();

        file.createNewFile();

        try (final OutputStream output = new FileOutputStream(file)) {
            properties.store(output, null);
        }
        catch (final IOException e) {
            throw (e);
        }
        return properties;
    }

    public static @Nullable Properties readDefaultProperties(final @NotNull String propertiesPath) throws Exception {
        final Properties properties = new Properties(DEFAULT_PROPERTIES);

        try (final InputStream input = new FileInputStream(propertiesPath)) {

            properties.load(input);

        }
        catch (final FileNotFoundException e) {
            return null;
        }
        catch (final Exception e) {
            throw (e);
        }

        return properties;
    }

    public static void setDefaultProperties(final @NotNull Properties properties) throws Exception {
        DEFAULT_MQTT_VERSION = new MqttVersionConverter().convert(properties.getProperty("mqtt.version"));
        DEFAULT_HOST = properties.getProperty("mqtt.host");
        DEFAULT_PORT = Integer.valueOf(properties.getProperty("mqtt.port"));
        DEFAULT_SHELL_DEBUG_LEVEL = DEBUG_LEVEL.valueOf(properties.getProperty("debug.level.shell"));
        DEFAULT_LOGFILE_PATH = properties.getProperty("debug.logfile.path");
        DEFAULT_CLIENT_PREFIX = properties.getProperty("client.prefix");
        DEFAULT_SUBSCRIBE_OUTPUT_FILE = properties.getProperty("client.subscribe.output");

        DEFAULT_PROPERTIES = properties;
    }

    public static void resetDefaultProperties() {
        DEFAULT_MQTT_VERSION = DefaultProperties.MQTT_VERSION;
        DEFAULT_HOST = DefaultProperties.HOST;
        DEFAULT_PORT = DefaultProperties.PORT;
        DEFAULT_SHELL_DEBUG_LEVEL = DefaultProperties.SHELL_DEBUG_LEVEL;
        DEFAULT_LOGFILE_PATH = DefaultProperties.LOGFILE_PATH;
        DEFAULT_CLIENT_PREFIX = DefaultProperties.CLIENT_PREFIX;
        DEFAULT_SUBSCRIBE_OUTPUT_FILE = DefaultProperties.SUBSCRIBE_OUTPUT_FILE;

        DEFAULT_PROPERTIES = getDefaultProperties();
    }

}
