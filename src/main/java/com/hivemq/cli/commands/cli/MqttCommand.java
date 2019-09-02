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
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

@CommandLine.Command()
public abstract class MqttCommand extends AbstractCommand implements CliCommand {


    @CommandLine.Option(names = {"-V", "--version"}, defaultValue = "5", converter = MqttVersionConverter.class, description = "The mqtt version used by the client (default: 5)")
    private MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker (default: 1883)")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    @Nullable
    private String identifier;

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

}
