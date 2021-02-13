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
package com.hivemq.cli.commands;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.options.SslOptions;
import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.EnvVarToByteBufferConverter;
import com.hivemq.cli.converters.PasswordFileToByteBufferConverter;
import com.hivemq.cli.converters.UnsignedShortConverter;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public abstract class AbstractCommonFlags extends AbstractConnectRestrictionFlags implements Connect {

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for authentication", order = 2)
    @Nullable
    private String user;

    @CommandLine.Option(names = {"-pw", "--password"}, arity = "0..1", interactive = true, converter = ByteBufferConverter.class, description = "The password for authentication", order = 2)
    @Nullable
    private ByteBuffer password;

    @CommandLine.Option(names = {"-pw:env"}, arity = "0..1", converter = EnvVarToByteBufferConverter.class, fallbackValue = "MQTT_CLI_PW", description = "The password for authentication read in from an environment variable", order = 2)
    private void setPasswordFromEnv(final @NotNull ByteBuffer passwordEnvironmentVariable) { password = passwordEnvironmentVariable; }

    @CommandLine.Option(names = {"-pw:file"}, converter = PasswordFileToByteBufferConverter.class, description = "The password for authentication read in from a file", order = 2)
    private void setPasswordFromFile(final @NotNull ByteBuffer passwordFromFile) { password = passwordFromFile; }

    @CommandLine.Option(names = {"-k", "--keepAlive"}, converter = UnsignedShortConverter.class, description = "A keep alive of the client (in seconds) (default: 60)", order = 2)
    private @Nullable Integer keepAlive;

    @CommandLine.Option(names = {"-c", "--cleanStart"}, negatable = true, description = "Define a clean start for the connection (default: true)", order = 2)
    @Nullable
    private Boolean cleanStart;

    @CommandLine.Mixin
    private final SslOptions sslOptions = new SslOptions();

    @CommandLine.Option(names = {"-ws"}, description = "Use WebSocket transport protocol (default: false)", order = 2)
    private boolean useWebSocket;

    @CommandLine.Option(names = {"-ws:path"}, description = "The path of the WebSocket", order = 2)
    @Nullable
    private String webSocketPath;

    @Override
    public void setDefaultOptions() {
        super.setDefaultOptions();
        final DefaultCLIProperties defaultCLIProperties = MqttCLIMain.MQTTCLI.defaultCLIProperties();

        if (user == null) {
            user = defaultCLIProperties.getUsername();
        }

        if (password == null) {
            try {
                password = defaultCLIProperties.getPassword();
            } catch (Exception e) {
                Logger.error(e, "Default password could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }


        if (useWebSocket && webSocketPath == null) {
            webSocketPath = defaultCLIProperties.getWebsocketPath();
        }

    }

    @Nullable
    public MqttClientSslConfig buildSslConfig() throws Exception {
        return sslOptions.buildSslConfig();
    }


    @Override
    public String toString() {

        return "Connect{" +
                "key=" + getKey() +
                ", " + commonOptions() +
                '}';
    }


    public String commonOptions() {
        return super.toString() +
                (user != null ? (", user=" + user) : "") +
                (keepAlive != null ? (", keepAlive=" + keepAlive) : "") +
                (cleanStart != null ? (", cleanStart=" + cleanStart) : "") +
                ", sslOptions=" + sslOptions +
                ", useWebSocket=" + useWebSocket +
                (webSocketPath != null ? (", webSocketPath=" + webSocketPath) : "") +
                getWillOptions();
    }

    @Nullable
    public String getUser() {
        return user;
    }

    public void setUser(final @Nullable String user) {
        this.user = user;
    }

    @Nullable
    public ByteBuffer getPassword() {
        return password;
    }

    @Nullable
    public Integer getKeepAlive() {
        return keepAlive;
    }

    @Nullable
    public Boolean getCleanStart() {
        return cleanStart;
    }

    @Nullable
    public MqttWebSocketConfig getWebSocketConfig() {
        if (useWebSocket) {
            return MqttWebSocketConfig.builder()
                    .serverPath(webSocketPath)
                    .build();
        } else {
            return null;
        }
    }

}
