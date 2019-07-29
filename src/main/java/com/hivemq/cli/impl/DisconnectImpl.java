package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.pmw.tinylog.Logger;

public class DisconnectImpl implements MqttAction {

    private static DisconnectImpl instance = null;
    private Disconnect param;

    private DisconnectImpl() {
    }

    public static DisconnectImpl get(final Disconnect param) {
        if (instance == null) {
            instance = new DisconnectImpl();
        }
        instance.setParam(param);
        return instance;
    }

    public Disconnect getParam() {
        return param;
    }

    private void setParam(Disconnect param) {
        this.param = param;
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            Logger.debug("Command: {} ", param);
        }

        try {
            MqttClientExecutor.getInstance().disconnect(param);
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
