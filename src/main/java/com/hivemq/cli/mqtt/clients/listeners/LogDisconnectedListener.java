package com.hivemq.cli.mqtt.clients.listeners;

import com.google.common.base.Throwables;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class LogDisconnectedListener implements MqttClientDisconnectedListener {

    public static final @NotNull LogDisconnectedListener INSTANCE = new LogDisconnectedListener();

    private LogDisconnectedListener() {
    }

    @Override
    public void onDisconnected(@NotNull final MqttClientDisconnectedContext context) {
        if (context.getSource() != MqttDisconnectSource.USER) {
            final Throwable cause = context.getCause();

            Logger.debug(cause,
                    "{} DISCONNECTED {}",
                    LoggerUtils.getClientPrefix(context.getClientConfig()),
                    Throwables.getRootCause(cause).getMessage());
        }
    }
}
