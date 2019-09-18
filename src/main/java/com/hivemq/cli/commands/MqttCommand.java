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

import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command()
public abstract class MqttCommand extends AbstractCommand implements Context {


    @CommandLine.Option(names = {"-V", "--mqttVersion"}, converter = MqttVersionConverter.class, description = "The mqtt version used by the client (default: 5)", order = 1)
    private MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')", order = 1)
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)", order = 1)
    private Integer port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)", order = 1)
    @Nullable
    private String identifier;

    @CommandLine.Option(names = {"-ip", "--identifierPrefix"}, description = "The prefix of the client Identifier UTF-8 String", order = 2)
    private String identifierPrefix;

    public void setDefaultOptions() {
        if (version == null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'version' to default value: {}", PropertiesUtils.DEFAULT_MQTT_VERSION);
            }
            version = PropertiesUtils.DEFAULT_MQTT_VERSION;
        }

        if (host == null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'host' to default value: {}", PropertiesUtils.DEFAULT_HOST);
            }
            host = PropertiesUtils.DEFAULT_HOST;
        }

        if (port == null) {
            if (isVerbose()) {
                Logger.trace("Setting value of 'port' to default value: {}", PropertiesUtils.DEFAULT_PORT);
            }
            port = PropertiesUtils.DEFAULT_PORT;
        }

        if (identifierPrefix == null) {
            identifierPrefix = PropertiesUtils.DEFAULT_CLIENT_PREFIX;
        }

        if (identifier == null) {
            identifier = createIdentifier();
            if (isVerbose()) {
                Logger.trace("Created 'identifier': {}", identifier);
            }
        }
    }

    public String createIdentifier() {
        return identifierPrefix + "-" + this.getVersion() + "-" + UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "host=" + host +
                ", port=" + port +
                ", version=" + version +
                ", identifier=" + identifier;
    }

    @Override
    public String getKey() {
        return "client {" +
                "identifier='" + getIdentifier() + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }

    public @NotNull MqttVersion getVersion() {
        return version;
    }

    public void setVersion(final @NotNull MqttVersion version) {
        this.version = version;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final @Nullable String identifier) {
        this.identifier = identifier;
    }

    @Nullable
    public String getIdentifierPrefix() {
        return identifierPrefix;
    }

    public void setIdentifierPrefix(final String identifierPrefix) {
        this.identifierPrefix = identifierPrefix;
    }
}
