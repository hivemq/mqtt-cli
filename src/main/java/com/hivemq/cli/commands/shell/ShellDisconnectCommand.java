package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.Context;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.cli.MqttCommand;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "dis",
        aliases = "disconnect",
        description = "Disconnects an mqtt client")

public class ShellDisconnectCommand implements MqttAction, Disconnect {

    private final MqttClientExecutor mqttClientExecutor;

    @Inject
    ShellDisconnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;

    }

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

    @CommandLine.Option(names = {"-up", "--userProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the disconnect message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')")
    @Nullable
    private Mqtt5UserProperties userProperties;

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
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());

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

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return userProperties;
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

    @NotNull
    public String getHost() {
        return host;
    }

    public void setHost(@NotNull final String host) {
        this.host = host;
    }

    public void setUserProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = userProperties;


    }
}
