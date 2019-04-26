package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.util.MqttUtils;

public class DisconnectImpl implements MqttAction {

    private static DisconnectImpl instance = new DisconnectImpl();
    private Disconnect param;

    private DisconnectImpl() {
    }

    public static DisconnectImpl get(final Disconnect param) {
        instance.param = param;
        return instance;
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            System.out.println(param);
        }

        try {
            MqttUtils.getInstance().disconnect(param);
        } catch (Exception others) {
            System.err.println(others.getMessage());
        }

    }

    @Override
    public String getKey() {
        return param.getKey();
    }
}
