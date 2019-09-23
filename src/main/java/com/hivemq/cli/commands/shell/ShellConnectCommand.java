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

import com.hivemq.cli.commands.AbstractCommonFlags;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "con",
        aliases = "connect",
        description = "Connects an mqtt client",
        abbreviateSynopsis = true)

public class ShellConnectCommand extends AbstractCommonFlags implements Runnable, Connect {

    private final MqttClientExecutor mqttClientExecutor;

    @Nullable
    public MqttClient client;

    //needed for pico cli - reflection code generation
    public ShellConnectCommand() {
        this(null);
    }

    @Inject
    public ShellConnectCommand(@NotNull final MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;
    }

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the session of the connected client'")
    @Nullable
    private Long sessionExpiryInterval;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the connect message")
    @Nullable
    private Mqtt5UserProperty[] connectUserProperties;


    public void run() {
        client = null;

        setDefaultOptions();

        handleCommonOptions();

        logUnusedOptions();

        connect();

        ShellContextCommand.updateContext(client);
    }

    private void connect() {
        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            client = mqttClientExecutor.connect(this);
        }
        catch (final Exception ex) {
            LoggingContext.put("identifier", "CONNECT");
            if (isVerbose()) {
                Logger.trace(ex.getStackTrace());
            }
            else if (isDebug()) {
                Logger.debug(ex.getMessage());
            }
            Logger.error(ex.getCause().getMessage());
        }
    }

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (sessionExpiryInterval != null) {
                Logger.warn("Connect session expiry interval was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (connectUserProperties != null) {
                Logger.warn("Connect user properties were set but are unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    String connectOptions() {
        return commonOptions() +
                ", sessionExpiryInterval= " + sessionExpiryInterval +
                ", userProperties=" + connectUserProperties +
                ", " + connectRestrictionOptions();
    }

    @Override
    public String toString() {
        return "ShellConnectCommand:: {" + connectOptions() + "}";
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Nullable
    public Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(@Nullable final Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Nullable
    public Mqtt5UserProperties getConnectUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(connectUserProperties);
    }

    public void setConnectUserProperties(@Nullable final Mqtt5UserProperty... connectUserProperties) {
        this.connectUserProperties = connectUserProperties;
    }
}
