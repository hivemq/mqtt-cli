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
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
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
        description = "Disconnects an mqtt client")

public class ShellDisconnectCommand implements MqttAction, Disconnect {

    private final MqttClientExecutor mqttClientExecutor;

    //needed for pico cli - reflection code generation
    public ShellDisconnectCommand() {
        this(null);
    }

    @Inject
    ShellDisconnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;

    }

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-i", "--identifier"}, required = true, description = "The client identifier UTF-8 String (default randomly generated string)")
    @NotNull
    private String identifier;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    @NotNull
    private String host;

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
    public boolean isVerbose() {
        return false;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            mqttClientExecutor.disconnect(this);
        }
        catch (final Exception ex) {
            LoggingContext.put("identifier", "PUBLISH");
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
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getKey() {
        return "client {" +
                "identifier='" + getIdentifier() + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }


    @Override
    public String toString() {
        return "Disconnect::" + getKey();
    }


    @Override
    public @Nullable Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public @Nullable String getReasonString() {
        return reasonString;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void setSessionExpiryInterval(final @Nullable Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public void setReasonString(final @Nullable String reasonString) {
        this.reasonString = reasonString;
    }

    public void setIdentifier(@NotNull final String identifier) {
        this.identifier = identifier;
    }


    public void setHost(@NotNull final String host) {
        this.host = host;
    }

    public void setUserProperties(final @Nullable Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;


    }
}
