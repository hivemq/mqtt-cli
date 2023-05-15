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

package com.hivemq.cli.commands.options;

import com.google.common.base.Joiner;
import com.google.common.primitives.Chars;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.converters.UnsignedShortConverter;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConnectOptions {

    @CommandLine.Option(names = {"-V", "--mqttVersion"},
                        converter = MqttVersionConverter.class,
                        description = "The MQTT version used by the client (default: 5)")
    private @Nullable MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"},
                        description = "The hostname of the message broker (default 'localhost')")
    private @Nullable String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)")
    private @Nullable Integer port;

    @CommandLine.Option(names = {"-i", "--identifier"},
                        description = "The client identifier UTF-8 String (default randomly generated string)")
    private @Nullable String identifier;

    @CommandLine.Option(names = {"-ip", "--identifierPrefix"},
                        description = "The prefix of the client Identifier UTF-8 String")
    private @Nullable String identifierPrefix;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-k", "--keepAlive"},
                        converter = UnsignedShortConverter.class,
                        description = "A keep alive of the client (in seconds) (default: 60)")
    private @Nullable Integer keepAlive;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--no-cleanStart"},
                        negatable = true,
                        defaultValue = "true",
                        description = "Define a clean start for the connection (default: true)")
    private boolean cleanStart;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"},
                        converter = UnsignedIntConverter.class,
                        description = "The lifetime of the session of the connected client")
    private @Nullable Long sessionExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-Cup", "--connectUserProperty"},
                        converter = Mqtt5UserPropertyConverter.class,
                        description = "A user property of the connect message'")
    private @Nullable Mqtt5UserProperty @Nullable [] connectUserProperties;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-ws"}, description = "Use WebSocket transport protocol (default: false)")
    private boolean useWebSocket;

    @CommandLine.Option(names = {"-ws:path"}, description = "The path of the WebSocket")
    private @Nullable String webSocketPath;

    @CommandLine.Mixin
    private final @NotNull WillOptions willOptions = new WillOptions();

    @CommandLine.Mixin
    private final @NotNull ConnectRestrictionOptions connectRestrictionOptions = new ConnectRestrictionOptions();

    @CommandLine.Mixin
    private final @NotNull AuthenticationOptions authenticationOptions = new AuthenticationOptions();

    @CommandLine.Mixin
    private final @NotNull TlsOptions tlsOptions = new TlsOptions();

    public @NotNull MqttVersion getVersion() {
        return Objects.requireNonNull(version);
    }

    public @NotNull String getHost() {
        return Objects.requireNonNull(host);
    }

    public int getPort() {
        return Objects.requireNonNull(port);
    }

    public @Nullable String getIdentifier() {
        return identifier;
    }

    public @Nullable Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public @Nullable Mqtt5UserProperties getConnectUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(connectUserProperties);
    }

    public @NotNull WillOptions getWillOptions() {
        return willOptions;
    }

    public @NotNull AuthenticationOptions getAuthenticationOptions() {
        return authenticationOptions;
    }

    public @NotNull ConnectRestrictionOptions getConnectRestrictionOptions() {
        return connectRestrictionOptions;
    }

    public @Nullable MqttClientSslConfig buildSslConfig() throws Exception {
        return tlsOptions.buildSslConfig();
    }

    public @Nullable Integer getKeepAlive() {
        return keepAlive;
    }

    public @Nullable Boolean getCleanStart() {
        return cleanStart;
    }

    public @Nullable MqttWebSocketConfig getWebSocketConfig() {
        if (useWebSocket) {
            return MqttWebSocketConfig.builder().serverPath(Objects.requireNonNull(webSocketPath)).build();
        } else {
            return null;
        }
    }

    public void setDefaultOptions() {
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();

        if (version == null) {
            version = defaultCLIProperties.getMqttVersion();
            Logger.trace("Setting value of 'version' to default value: {}", version);
        }
        if (host == null) {
            host = defaultCLIProperties.getHost();
            Logger.trace("Setting value of 'host' to default value: {}", host);
        }
        if (port == null) {
            port = defaultCLIProperties.getPort();
            Logger.trace("Setting value of 'port' to default value: {}", port);
        }
        if (identifierPrefix == null) {
            identifierPrefix = defaultCLIProperties.getClientPrefix();
        }
        if (identifier == null) {
            if (version == MqttVersion.MQTT_5_0) {
                identifier = "";
                Logger.trace("Empty identifier will lead to using broker generated client identifier");
            } else {
                final String rndID = MqttUtils.buildRandomClientID(defaultCLIProperties.getClientLength());
                identifier = identifierPrefix + rndID;
                Logger.trace("Created identifier ('{}')", identifier);
            }
        }

        if (useWebSocket && webSocketPath == null) {
            webSocketPath = defaultCLIProperties.getWebsocketPath();
        }

        authenticationOptions.setDefaultOptions();
    }

    public void logUnusedOptions() {
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (sessionExpiryInterval != null) {
                Logger.warn("Connect session expiry interval was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }

            if (connectUserProperties != null) {
                Logger.warn("Connect user properties were set but are unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }

        if (version == MqttVersion.MQTT_5_0 && Objects.requireNonNull(identifier).isEmpty()) {
            // Client identifier will be generated by broker so no warning needs to be printed
            return;
        }
        final List<MqttUtils.IdentifierWarning> warnings =
                MqttUtils.getIdentifierWarnings(Objects.requireNonNull(identifier));

        for (final MqttUtils.IdentifierWarning warning : warnings) {
            switch (warning) {
                case TOO_LONG:
                    Logger.warn("Identifier '{}' may be too long (identifier length '{}' exceeds 23)",
                            identifier,
                            identifier.length());
                    break;
                case TOO_SHORT:
                    Logger.warn("Identifier '{}' may be too short (identifier length '{}' is less than 1)",
                            identifier,
                            identifier.length());
                    break;
                case CONTAINS_INVALID_CHAR:
                    final char[] invalidChars = MqttUtils.getInvalidIdChars(identifier);
                    Logger.warn("Identifier '{}' may contain invalid characters ({})",
                            identifier,
                            "'" + Joiner.on("', '").join(Chars.asList(invalidChars)) + "'");
                    break;
            }
        }

        if (version != null) {
            willOptions.logUnusedOptions(version);
            connectRestrictionOptions.logUnusedOptions(version);
        }
    }

    @Override
    public @NotNull String toString() {
        return "ConnectOptions{" +
                "version=" +
                version +
                ", host='" +
                host +
                '\'' +
                ", port=" +
                port +
                ", identifier='" +
                identifier +
                '\'' +
                ", identifierPrefix='" +
                identifierPrefix +
                '\'' +
                ", keepAlive=" +
                keepAlive +
                ", cleanStart=" +
                cleanStart +
                ", sessionExpiryInterval=" +
                sessionExpiryInterval +
                ", connectUserProperties=" +
                Arrays.toString(connectUserProperties) +
                ", useWebSocket=" +
                useWebSocket +
                ", webSocketPath='" +
                webSocketPath +
                '\'' +
                ", willOptions=" +
                willOptions +
                ", connectRestrictionOptions=" +
                connectRestrictionOptions +
                ", authenticationOptions=" +
                authenticationOptions +
                ", tlsOptions=" +
                tlsOptions +
                '}';
    }
}
