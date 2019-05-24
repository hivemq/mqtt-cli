package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.util.MqttClientUtils;
import org.pmw.tinylog.Logger;

public class ConnectionImpl implements MqttAction {

    private static ConnectionImpl instance = null;
    private Connect param;

    private ConnectionImpl(Connect connect) {
        this.param = connect;
    }

    public static ConnectionImpl get(final Connect param) {
        if (instance == null) {
            instance = new ConnectionImpl(param);
        }
        return instance;
    }

    @Override
    public void run() {
        if (param.isDebug()) {
            Logger.debug("Command: {} ", param);
        }

        try {
            MqttClientUtils.getInstance().connect(param);
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
