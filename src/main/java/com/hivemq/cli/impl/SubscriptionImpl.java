package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.pmw.tinylog.Logger;

public class SubscriptionImpl implements MqttAction {

    private static SubscriptionImpl instance = null;
    private Subscribe param;

    private SubscriptionImpl() {
    }


    public static SubscriptionImpl get(final Subscribe param) {
        if (instance == null) {
            instance = new SubscriptionImpl();
        }
        instance.setParam(param);
        return instance;
    }

    private void setParam(Subscribe param) {
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
            MqttClientExecutor.getInstance().subscribe(param);
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
                System.out.println(param.getIdentifier());
                param.wait(5000);
            }
            Logger.debug("Client disconnected.");
        }
    }
}
