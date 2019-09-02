package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.Context;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.converters.UserPropertiesConverter;
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
        description = "Disconnects this mqtt client")

public class ContextDisconnectCommand extends ShellContextCommand implements Runnable, Disconnect {

    @Inject
    public ContextDisconnectCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

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
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        logUnusedDisconnectOptions();

        try {
            mqttClientExecutor.disconnect(this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
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
    public Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(@Nullable final Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Nullable
    public String getReasonString() {
        return reasonString;
    }

    public void setReasonString(@Nullable final String reasonString) {
        this.reasonString = reasonString;
    }

    public Mqtt5UserProperties getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(@Nullable final Mqtt5UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
