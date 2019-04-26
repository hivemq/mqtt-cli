package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.util.MqttUtils;

public class PublishImpl implements MqttAction {

    private static PublishImpl instance = new PublishImpl();
    private Publish param;

    private PublishImpl() {
    }

    public static PublishImpl get(final Publish param) {
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
            MqttUtils.getInstance().publish(param);
        } catch (Exception others) {
            System.err.println(others.getMessage());
        }

    }

}
