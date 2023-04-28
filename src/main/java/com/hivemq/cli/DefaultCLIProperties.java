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

package com.hivemq.cli;

import com.hivemq.cli.converters.EnvVarToByteBufferConverter;
import com.hivemq.cli.converters.PasswordFileToByteBufferConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Level;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents the default properties which are used throughout the CLI. This class pre defines values for every default
 * property which can be overwritten by using the properties file given in the constructor.
 * <p>
 * The CLI uses the default location of the properties file at `~/.mqtt-cli/config.properties` which will be written by
 * the 'init' method if not present or else the file defined properties will overwrite the pre-defined properties.
 */
@Singleton
public class DefaultCLIProperties {

    private static final @NotNull String MQTT_VERSION = "mqtt.version";
    private static final @NotNull String HOST = "mqtt.host";
    private static final @NotNull String PORT = "mqtt.port";
    private static final @NotNull String LOGFILE_DEBUG_LEVEL = "logfile.level";
    private static final @NotNull String CLIENT_ID_PREFIX = "client.id.prefix";
    private static final @NotNull String CLIENT_ID_LENGTH = "client.id.length";
    private static final @NotNull String SUBSCRIBE_OUTPUT_FILE = "client.subscribe.output";
    private static final @NotNull String LOGFILE_PATH = "logfile.path";
    private static final @NotNull String USERNAME = "auth.username";
    private static final @NotNull String PASSWORD = "auth.password";
    private static final @NotNull String PASSWORD_FILE = "auth.password.file";
    private static final @NotNull String PASSWORD_ENV = "auth.password.env";
    private static final @NotNull String CLIENT_CERTIFICATE = "auth.client.cert";
    private static final @NotNull String CLIENT_PRIVATE_KEY = "auth.client.key";
    private static final @NotNull String CLIENT_PRIVATE_KEY_PASSWORD = "auth.client.key.password";
    private static final @NotNull String SERVER_CERTIFICATE = "auth.server.cafile";
    private static final @NotNull String KEYSTORE = "auth.keystore";
    private static final @NotNull String KEYSTORE_PASSWORD = "auth.keystore.password";
    private static final @NotNull String KEYSTORE_PRIVATE_KEY_PASSWORD = "auth.keystore.privatekey.password";
    private static final @NotNull String TRUSTSTORE = "auth.truststore";
    private static final @NotNull String TRUSTSTORE_PASSWORD = "auth.truststore.password";
    private static final @NotNull String WEBSOCKET_PATH = "ws.path";

    private final @NotNull Map<String, String> propertyToValue = new HashMap<String, String>() {{
        put(MQTT_VERSION, "5");
        put(HOST, "localhost");
        put(PORT, "1883");
        put(LOGFILE_DEBUG_LEVEL, "debug");
        put(CLIENT_ID_PREFIX, "mqtt");
        put(CLIENT_ID_LENGTH, "8");
        put(SUBSCRIBE_OUTPUT_FILE, null);
        put(LOGFILE_PATH, Paths.get(System.getProperty("user.home"), ".mqtt-cli", "logs").toString());
        put(USERNAME, null);
        put(PASSWORD, null);
        put(PASSWORD_FILE, null);
        put(PASSWORD_ENV, null);
        put(CLIENT_CERTIFICATE, null);
        put(CLIENT_PRIVATE_KEY, null);
        put(CLIENT_PRIVATE_KEY_PASSWORD, null);
        put(SERVER_CERTIFICATE, null);
        put(KEYSTORE, null);
        put(KEYSTORE_PASSWORD, null);
        put(KEYSTORE_PRIVATE_KEY_PASSWORD, null);
        put(TRUSTSTORE, null);
        put(TRUSTSTORE_PASSWORD, null);
        put(WEBSOCKET_PATH, "/mqtt");
    }};

    private final @NotNull File storePropertiesFile;

    /**
     * A singleton instance of this class holds reference to a properties file which will be written or created with the
     * 'init' method
     *
     * @param filePath the path to where the properties file shall be written or read from
     */
    public DefaultCLIProperties(final @NotNull Path filePath) {
        storePropertiesFile = filePath.toFile();
    }

    /**
     * Initializes the default properties from the file. If the file does not yet exist it will be created and
     * populated with the pre-defined values. Else the properties from the given file will be read which will override
     * the pre-defined values if given.
     *
     * @throws IOException              the creation or reading of the properties file failed
     * @throws IllegalArgumentException the path to the properties file is not valid
     */
    void init() throws IOException, IllegalArgumentException {
        if (!storePropertiesFile.exists()) {
            createFile();
        } else if (!storePropertiesFile.isFile()) {
            throw new IllegalArgumentException("The given file path does not lead to a valid properties file ('" +
                    storePropertiesFile.getPath() +
                    "')");
        } else {
            readFromFile();
        }
    }

    private void readFromFile() throws IOException {
        final Properties fileProperties = new Properties();

        try (final InputStream input = Files.newInputStream(storePropertiesFile.toPath())) {
            fileProperties.load(input);
        }

        fileProperties.stringPropertyNames()
                .stream()
                .filter(propertyToValue::containsKey)
                .distinct()
                .forEach(name -> propertyToValue.put(name, fileProperties.getProperty(name)));
    }

    private void createFile() throws IOException {
        //noinspection ResultOfMethodCallIgnored
        storePropertiesFile.getParentFile().mkdirs();
        assert storePropertiesFile.createNewFile();
        try (final OutputStream output = Files.newOutputStream(storePropertiesFile.toPath())) {
            final Properties properties = getProperties();
            properties.store(output, null);
        }

    }

    private Properties getProperties() {
        final Properties properties = new Properties();

        propertyToValue.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .distinct()
                .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

        return properties;
    }

    public @Nullable File getFile() {
        return storePropertiesFile;
    }

    public @NotNull MqttVersion getMqttVersion() {
        final String versionString = propertyToValue.get(MQTT_VERSION);
        switch (versionString) {
            case "5":
                return MqttVersion.MQTT_5_0;
            case "3":
                return MqttVersion.MQTT_3_1_1;
        }
        throw new IllegalArgumentException("'" + versionString + "' is not a valid MQTT version");
    }

    public @NotNull String getHost() {
        return propertyToValue.get(HOST);
    }

    public int getPort() {
        return Integer.parseInt(propertyToValue.get(PORT));
    }

    public @NotNull Level getLogfileDebugLevel() {
        final String debugLevel = propertyToValue.get(LOGFILE_DEBUG_LEVEL);
        switch (debugLevel.toLowerCase()) {
            case "trace":
            case "verbose":
                return Level.TRACE;
            case "debug":
                return Level.DEBUG;
            case "info":
                return Level.INFO;
            case "warn":
                return Level.WARN;
            case "error":
                return Level.ERROR;
        }
        throw new IllegalArgumentException("'" + debugLevel + "' is not a valid debug level");
    }

    public @NotNull String getClientPrefix() {
        return propertyToValue.get(CLIENT_ID_PREFIX);
    }

    public int getClientLength() {
        return Integer.parseInt(propertyToValue.get(CLIENT_ID_LENGTH));
    }

    public @Nullable String getClientSubscribeOutputFile() {
        return propertyToValue.get(SUBSCRIBE_OUTPUT_FILE);
    }

    public @NotNull Path getLogfilePath() {
        return Paths.get(propertyToValue.get(LOGFILE_PATH));
    }

    public @Nullable String getUsername() {
        return propertyToValue.get(USERNAME);
    }

    public @Nullable ByteBuffer getPassword() throws Exception {
        final String passwordText = propertyToValue.get(PASSWORD);
        final String passwordFile = propertyToValue.get(PASSWORD_FILE);
        final String passwordFromEnv = propertyToValue.get(PASSWORD_ENV);
        ByteBuffer password = null;

        if (passwordText != null) {
            password = ByteBuffer.wrap(passwordText.getBytes());
        }

        if (passwordFile != null) {
            password = new PasswordFileToByteBufferConverter().convert(passwordFile);
        }

        if (passwordFromEnv != null) {
            password = new EnvVarToByteBufferConverter().convert(passwordFromEnv);
        }

        return password;
    }

    public @Nullable Path getClientCertificateChain() {
        final String clientCertificate = propertyToValue.get(CLIENT_CERTIFICATE);
        if (clientCertificate == null) {
            return null;
        }
        return Paths.get(clientCertificate);
    }

    public @Nullable Path getClientPrivateKey() {
        final String clientPrivateKey = propertyToValue.get(CLIENT_PRIVATE_KEY);
        if (clientPrivateKey == null) {
            return null;
        }
        return Paths.get(clientPrivateKey);
    }

    public @Nullable String getClientPrivateKeyPassword() {
        return propertyToValue.get(CLIENT_PRIVATE_KEY_PASSWORD);
    }

    public @Nullable Path getServerCertificateChain() {
        final String serverCertificate = propertyToValue.get(SERVER_CERTIFICATE);
        if (serverCertificate == null) {
            return null;
        }
        return Paths.get(serverCertificate);
    }

    public @Nullable Path getKeystore() {
        final String keystore = propertyToValue.get(KEYSTORE);
        if (keystore == null) {
            return null;
        }
        return Paths.get(keystore);
    }

    public @Nullable String getKeystorePassword() {
        return propertyToValue.get(KEYSTORE_PASSWORD);
    }

    public @Nullable String getKeystorePrivateKeyPassword() {
        return propertyToValue.get(KEYSTORE_PRIVATE_KEY_PASSWORD);
    }

    public @Nullable Path getTruststore() {
        final String truststore = propertyToValue.get(TRUSTSTORE);
        if (truststore == null) {
            return null;
        }
        return Paths.get(truststore);
    }

    public @Nullable String getTruststorePassword() {
        return propertyToValue.get(TRUSTSTORE_PASSWORD);
    }

    public @NotNull String getWebsocketPath() {
        return propertyToValue.get(WEBSOCKET_PATH);
    }

}
