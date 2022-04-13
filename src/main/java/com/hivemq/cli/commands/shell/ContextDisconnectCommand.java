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
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnects this MQTT client")
public class ContextDisconnectCommand extends ShellContextCommand implements Runnable, Disconnect {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-a", "--all"}, defaultValue = "false",
            description = "Disconnect all connected clients")
    private boolean disconnectAll;

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

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextDisconnectCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ContextDisconnectCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @Override
    public void run() {
        Logger.trace("Command {} ", this);

        logUnusedDisconnectOptions();

        try {
            if (disconnectAll) {
                mqttClientExecutor.disconnectAllClients(this);
            } else {
                mqttClientExecutor.disconnect(this);
            }
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "key=" + getKey() + ", all=" + disconnectAll +
                (sessionExpiryInterval != null ? (", sessionExpiryInterval=" + sessionExpiryInterval) : "") +
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

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    private void logUnusedDisconnectOptions() {
        if (Objects.requireNonNull(contextClient).getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (sessionExpiryInterval != null) {
                Logger.warn("Session expiry interval set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }

            if (reasonString != null) {
                Logger.warn("Reason string was set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }

            if (userProperties != null) {
                Logger.warn("User properties were set but are unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }
}
