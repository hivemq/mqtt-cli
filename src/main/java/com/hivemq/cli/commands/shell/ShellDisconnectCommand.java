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

import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.LoggerUtils;
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
    private final DefaultCLIProperties defaultCLIProperties;

    //needed for pico cli - reflection code generation
    public ShellDisconnectCommand() {
        this(null, null);
    }

    @Inject
    ShellDisconnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor,
                           final @NotNull DefaultCLIProperties defaultCLIProperties) {

        this.mqttClientExecutor = mqttClientExecutor;
        this.defaultCLIProperties = defaultCLIProperties;

    }

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-a", "--all"}, defaultValue = "false", description = "Disconnect all connected clients")
    private boolean disconnectAll;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    @Nullable private String identifier;

    @CommandLine.Option(names = {"-h", "--host"}, description = "The hostname of the message broker (default 'localhost')")
    @Nullable private String host;

    @CommandLine.Option(names = {"-e", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The session expiry of the disconnect (default: 0)")
    @Nullable private Long sessionExpiryInterval;

    @CommandLine.Option(names = {"-r", "--reason"}, description = "The reason of the disconnect")
    @Nullable private String reasonString;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the disconnect message")
    @Nullable private Mqtt5UserProperty[] userProperties;

    @Override
    public void run() {

        if (host == null) {
            host = defaultCLIProperties.getHost();
        }

        //TODO
        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            if (disconnectAll) {
                mqttClientExecutor.disconnectAllClients(this);
            }
            else {
                if (identifier == null) {
                    Logger.error("Missing required option '--identifier=<identifier>'");
                    return;
                }

                mqttClientExecutor.disconnect(this);
            }
        }
        catch (final Exception ex) {
            LoggerUtils.logOnRightLevels(this, ex);
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

    public void setUserProperties(final @Nullable Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
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
