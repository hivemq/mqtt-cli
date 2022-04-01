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

package com.hivemq.cli.commands.shell;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.MqttAction;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnect an MQTT client")
public class ShellDisconnectCommand implements MqttAction, Disconnect {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-a", "--all"}, defaultValue = "false",
            description = "Disconnect all connected clients")
    private boolean disconnectAll;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--identifier"},
            description = "The client identifier UTF-8 String (default randomly generated string)")
    private @Nullable String identifier;

    @CommandLine.Option(names = {"-h", "--host"},
            description = "The hostname of the message broker (default 'localhost')")
    private @Nullable String host;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-e", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class,
            description = "The session expiry of the disconnect (default: 0)")
    private @Nullable Long sessionExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-r", "--reason"}, description = "The reason of the disconnect")
    private @Nullable String reasonString;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property of the disconnect message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    private final @NotNull MqttClientExecutor mqttClientExecutor;
    private final @NotNull DefaultCLIProperties defaultCLIProperties;

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ShellDisconnectCommand() {
        //noinspection ConstantConditions
        this(null, null);
    }

    @Inject
    ShellDisconnectCommand(
            final @NotNull MqttClientExecutor mqttClientExecutor,
            final @NotNull DefaultCLIProperties defaultCLIProperties) {
        this.mqttClientExecutor = mqttClientExecutor;
        this.defaultCLIProperties = defaultCLIProperties;
    }

    @Override
    public void run() {
        if (host == null) {
            host = defaultCLIProperties.getHost();
        }
        Logger.trace("Command {} ", this);

        try {
            if (disconnectAll) {
                mqttClientExecutor.disconnectAllClients(this);
            } else {
                if (identifier == null) {
                    Logger.error("Missing required option '--identifier=<identifier>'");
                    return;
                }
                mqttClientExecutor.disconnect(this);
            }
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return Objects.requireNonNull(identifier);
    }

    @Override
    public @NotNull String getKey() {
        return "client {" + "identifier='" + getIdentifier() + '\'' + ", host='" + getHost() + '\'' + '}';
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "disconnectAll=" + disconnectAll +
                (identifier != null ? (", identifier=" + identifier) : "") + (host != null ? (", host=" + host) : "") +
                (sessionExpiryInterval != null ? (", sessionExpiryInterval=" + host) : "") +
                (reasonString != null ? (", reasonString=" + reasonString) : "") +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") + "}";

    }

    @Override
    public @Nullable Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public @Nullable String getReasonString() {
        return reasonString;
    }

    public @NotNull String getHost() {
        return Objects.requireNonNull(host);
    }

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }
}
