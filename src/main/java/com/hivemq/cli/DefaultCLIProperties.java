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


import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Level;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Singleton
public final class DefaultCLIProperties {

    private static final String FILE_PATH =
            System.getProperty("user.home") + File.separator +
            ".mqtt-cli" + File.separator +
            "config.properties";

    private static final String MQTT_VERSION = "mqtt.version";
    private static final String HOST = "mqtt.host";
    private static final String PORT = "mqtt.port";
    private static final String DEBUG_LEVEL_SHELL = "debug.level.shell";
    private static final String CLIENT_PREFIX = "client.prefix";
    private static final String SUBSCRIBE_OUTPUT_FILE = "client.subscribe.output";
    private static final String LOGFILE_PATH = "logfile.path";

    private Map<String, String> propertyToValue = new HashMap<String, String>() {{
       put(MQTT_VERSION, "5");
       put(HOST, "localhost");
       put(PORT, "1883");
       put(DEBUG_LEVEL_SHELL, "verbose");
       put(CLIENT_PREFIX, "mqttClient");
       put(SUBSCRIBE_OUTPUT_FILE, null);
       put(LOGFILE_PATH, System.getProperty("user.home") + File.separator +
                        ".mqtt-cli" + File.separator +
                        "logs" + File.separator);
    }};

    private File storePropertiesFile = new File(FILE_PATH);

    @Inject public DefaultCLIProperties() {}

    public void readFromFile() throws IOException {
        final Properties fileProperties = new Properties();

        try (final InputStream input = new FileInputStream(FILE_PATH)) {
            fileProperties.load(input);
        }

        fileProperties.stringPropertyNames()
                .forEach(name -> propertyToValue.put(name, fileProperties.getProperty(name)));
    }

    public void createFile() throws IOException {
        if (!storePropertiesFile.exists()) {
            assert storePropertiesFile.getParentFile().mkdirs();
            assert storePropertiesFile.createNewFile();
            try (final OutputStream output = new FileOutputStream(storePropertiesFile)) {
                final Properties properties = getProperties();
                properties.store(output, null);
            }
        }
    }

    public Properties getProperties() {
        final Properties properties = new Properties();

        propertyToValue.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .distinct()
                .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

        return properties;
    }

    @Nullable
    public File getFile() {
        return storePropertiesFile;
    }

    /****************
     * Getter for properties in the concrete data types *
     ***************/

    @NotNull
    public MqttVersion getMqttVersion() {
        final String versionString = propertyToValue.get(MQTT_VERSION);
        switch (versionString) {
            case "5": return MqttVersion.MQTT_5_0;
            case "3": return MqttVersion.MQTT_3_1_1;
        }
        throw new IllegalArgumentException("'" + versionString + "' is not a valid MQTT version");
    }

    @NotNull
    public String getHost() {
        return propertyToValue.get(HOST);
    }

    public int getPort() {
        return Integer.parseInt(propertyToValue.get(PORT));
    }

    @NotNull
    public Level getShellDebugLevel() {
        final String shellDebugLevel = propertyToValue.get(DEBUG_LEVEL_SHELL);
        switch (shellDebugLevel.toLowerCase()) {
            case "verbose": return  Level.TRACE;
            case "debug": return Level.DEBUG;
            case "info": return Level.INFO;
        }
        throw new IllegalArgumentException("'" + shellDebugLevel + "' is not a valid debug level");
    }

    @NotNull
    public String getClientPrefix() {
        return propertyToValue.get(CLIENT_PREFIX);
    }

    @Nullable
    public String getClientSubscribeOutputFile() {
        return propertyToValue.get(SUBSCRIBE_OUTPUT_FILE);
    }

    @NotNull
    public String getLogfilePath() {
        return propertyToValue.get(LOGFILE_PATH);
    }


}
