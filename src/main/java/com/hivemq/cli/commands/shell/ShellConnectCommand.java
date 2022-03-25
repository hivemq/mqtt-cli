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
import com.hivemq.cli.commands.AbstractCommonFlags;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;

@CommandLine.Command(name = "con", aliases = "connect", description = "Connect an MQTT client",
        abbreviateSynopsis = true)
public class ShellConnectCommand extends AbstractCommonFlags implements Runnable, Connect {

    private final @NotNull MqttClientExecutor mqttClientExecutor;

    private @Nullable MqttClientSslConfig sslConfig;

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ShellConnectCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ShellConnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class,
            description = "The lifetime of the session of the connected client'")
    private @Nullable Long sessionExpiryInterval;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property of the connect message")
    private @Nullable Mqtt5UserProperty @Nullable [] connectUserProperties;

    public void run() {
        setDefaultOptions();

        try {
            sslConfig = buildSslConfig();
        } catch (final Exception e) {
            Logger.error(e, "Could not build SSL configuration");
            return;
        }

        logUnusedOptions();
        final MqttClient client = connect();

        sslConfig = null;
        ShellContextCommand.updateContext(client);
    }

    private @Nullable MqttClient connect() {
        Logger.trace("Command {} ", this);

        try {
            return mqttClientExecutor.connect(this);
        } catch (final ConnectionFailedException cex) {
            Logger.error(cex, cex.getCause().getMessage());
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
        return null;
    }

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();
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
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + connectOptions() + "}";
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public @Nullable Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public @Nullable Mqtt5UserProperties getConnectUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(connectUserProperties);
    }

    @Override
    public @Nullable MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }

    private @NotNull String connectOptions() {
        return commonOptions() +
                (sessionExpiryInterval != null ? (", sessionExpiryInterval=" + sessionExpiryInterval) : "") +
                (connectUserProperties != null ? (", userProperties=" + Arrays.toString(connectUserProperties)) : "") +
                connectRestrictionOptions();
    }
}
