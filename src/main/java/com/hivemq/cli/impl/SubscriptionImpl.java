package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SubscriptionImpl implements MqttAction {

    private static SubscriptionImpl instance = null;
    private final MqttClientExecutor mqttClientExecutor;
    private Subscribe param;

    @Inject
    private SubscriptionImpl(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }


    public static SubscriptionImpl get(final Subscribe param) {
        if (instance == null) {
            instance = new SubscriptionImpl();
        }
        instance.setParam(param);
        return instance;
    }

    public void setParam(Subscribe param) {
        this.param = param;
    }

    @Override
    public String getKey() {
        return param.getKey();
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            Logger.debug("Command: {} ", param);
        }

        try {
            mqttClientExecutor.subscribe(param);
        } catch (Exception ex) {
            if (param.isDebug()) {
                Logger.error(ex);
            } else {
                Logger.error(ex.getMessage());
            }
        }
    }

    public void stay() throws InterruptedException {
        synchronized (param) {
            while (MqttClientExecutor.getInstance().isConnected(param)) {
                param.wait(500);
            }
            Logger.debug("Client disconnected.");
        }
    }
}
