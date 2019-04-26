package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.util.MqttUtils;

public class ConnectionImpl implements MqttAction {

    private static ConnectionImpl instance = new ConnectionImpl();
    private Connect param;

    private ConnectionImpl() {
    }

    public static ConnectionImpl get(final Connect param) {
        instance.param = param;
        return instance;
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            System.out.println(param);
        }

        try {
            MqttUtils.getInstance().connect(param);
        } catch (Exception others) {
            System.err.println(others.getMessage());
        }

    }

    @Override
    public String getKey() {
        return param.getKey();
    }
}
