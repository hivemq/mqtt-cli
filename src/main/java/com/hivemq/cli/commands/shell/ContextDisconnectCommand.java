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
package com.hivemq.cli.commands.shell;

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
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "dis",
        aliases = "disconnect",
        description = "Disconnects this mqtt client")

public class ContextDisconnectCommand extends ShellContextCommand implements Runnable, Disconnect {

    //needed for pico cli - reflection code generation
    public ContextDisconnectCommand() {
        this(null);
    }

    @Inject
    public ContextDisconnectCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-e", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The session expiry of the disconnect (default: 0)")
    @Nullable
    private Long sessionExpiryInterval;

    @CommandLine.Option(names = {"-r", "--reason"}, description = "The reason of the disconnect")
    @Nullable
    private String reasonString;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the disconnect message")
    @Nullable
    private Mqtt5UserProperty[] userProperties;

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        logUnusedDisconnectOptions();

        try {
            mqttClientExecutor.disconnect(this);
        }
        catch (final Exception ex) {
            LoggingContext.put("identifier", "DISCONNECT");
            if (isVerbose()) {
                Logger.trace(ex.getStackTrace());
            }
            else if (isDebug()) {
                Logger.debug(ex.getMessage());
            }
            Logger.error(MqttUtils.getRootCause(ex).getMessage());
        }

        removeContext();

    }

    private void logUnusedDisconnectOptions() {
        if (contextClient.getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
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

    @Override
    public String toString() {
        return "ContextDisconnect:: {" +
                "key=" + getKey() +
                "}";
    }

    @Nullable
    @Override
    public Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(@Nullable final Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Nullable
    @Override
    public String getReasonString() {
        return reasonString;
    }

    public void setReasonString(@Nullable final String reasonString) {
        this.reasonString = reasonString;
    }

    @Nullable
    @Override
    public Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void setUserProperties(@Nullable final Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
    }
}
