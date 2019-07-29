package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.pmw.tinylog.Logger;

public class ConnectionImpl implements MqttAction {

    private static ConnectionImpl instance = null;
    private Connect param;

    private ConnectionImpl() {
    }

    public static ConnectionImpl get(final Connect param) {
        if (instance == null) {
            instance = new ConnectionImpl();
        }
        instance.setParam(param);
        return instance;
    }

    public Connect getParam() {
        return param;
    }

    private void setParam(Connect param) {
        this.param = param;
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            Logger.debug("Command: {} ", param);
        }

        try {
            MqttClientExecutor.getInstance().connect(param);
        } catch (Exception ex) {
            if (param.isDebug()) {
                Logger.error(ex);
            } else {
                Logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public String getKey() {
        return param.getKey();
    }
}
