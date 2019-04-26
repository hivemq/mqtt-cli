package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.util.MqttUtils;

public class SubscriptionImpl implements MqttAction {

    private static SubscriptionImpl instance = new SubscriptionImpl();
    private Subscribe param;

    private SubscriptionImpl() {
    }

    public static SubscriptionImpl get(final Subscribe param) {
        instance.param = param;
        return instance;
    }

    @Override
    public String getKey() {
        return param.getKey();
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            System.out.println(param);
        }

        try {
            MqttUtils.getInstance().subscribe(param);
        } catch (Exception others) {
            System.err.println(others.getMessage());
        }

    }

}
