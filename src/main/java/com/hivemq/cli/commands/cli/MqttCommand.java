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
package com.hivemq.cli.commands.cli;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.utils.PropertiesUtils;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command()
public abstract class MqttCommand extends AbstractCommand implements CliCommand {


    @CommandLine.Option(names = {"-V", "--version"}, converter = MqttVersionConverter.class, description = "The mqtt version used by the client (default: 5)")
    private MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "The port of the message broker (default: 1883)")
    private Integer port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    @Nullable
    private String identifier;

    @CommandLine.Option(names = {"-pi", "--prefixIdentifier"}, description = "The prefix of the client Identifier UTF-8 String")
    private String prefixIdentifier;

    public void setDefaultOptions() {
        if (version == null) {
            version = PropertiesUtils.DEFAULT_MQTT_VERSION;
        }

        if (host == null) {
            host = PropertiesUtils.DEFAULT_HOST;
        }

        if (port == null) {
            port = PropertiesUtils.DEFAULT_PORT;
        }

        if (prefixIdentifier == null) {
            prefixIdentifier = PropertiesUtils.DEFAULT_CLIENT_PREFIX;
        }

        if (identifier == null) {
            identifier = createIdentifier();
        }
    }

    public String createIdentifier() {
        if (getIdentifier() == null) {
            this.setIdentifier(prefixIdentifier + "-" + this.getVersion() + "-" + UUID.randomUUID().toString());
        }
        return getIdentifier();
    }

    @Override
    public String toString() {
        return "host=" + host +
                ", port=" + port +
                ", version=" + version +
                ", identifier=" + identifier;
    }

    public @NotNull MqttVersion getVersion() {
        return version;
    }

    public void setVersion(final @NotNull MqttVersion version) {
        this.version = version;
    }

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final @Nullable String identifier) {
        this.identifier = identifier;
    }

    public String getPrefixIdentifier() {
        return prefixIdentifier;
    }

    public void setPrefixIdentifier(final String prefixIdentifier) {
        this.prefixIdentifier = prefixIdentifier;
    }
}
