package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.pmw.tinylog.Logger;

public class PublishImpl implements MqttAction {

    private static PublishImpl instance = null;
    private Publish param;

    private PublishImpl() {
    }

    public static PublishImpl get(final Publish param) {
        if (instance == null) {
            instance = new PublishImpl();
        }
        instance.setParam(param);
        return instance;
    }

    public Publish getParam() {
        return param;
    }

    public void setParam(Publish param) {
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
            MqttClientExecutor.getInstance().publish(param);
        } catch (Exception ex) {
            if (param.isDebug()) {
                Logger.error(ex);
            } else {
                Logger.error(ex.getMessage());
            }
        }
    }

}
