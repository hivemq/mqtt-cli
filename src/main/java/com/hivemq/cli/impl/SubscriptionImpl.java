package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.util.MqttClientUtils;
import org.pmw.tinylog.Logger;

public class SubscriptionImpl implements MqttAction {

    private static SubscriptionImpl instance = null;
    private Subscribe param;

    private SubscriptionImpl(Subscribe subscribe) {
        this.param = subscribe;
    }

    public static SubscriptionImpl get(final Subscribe param) {
        if (instance == null) {
            instance = new SubscriptionImpl(param);
        }
        return instance;
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
            MqttClientUtils.getInstance().subscribe(param);
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
            while (MqttClientUtils.getInstance().isConnected(param)) {
                System.out.println(param.getIdentifier());
                param.wait(5000);
            }
            Logger.debug("Client disconnected.");
        }
    }
}
