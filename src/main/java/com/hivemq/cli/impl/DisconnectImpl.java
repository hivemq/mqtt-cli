package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.util.MqttClientUtils;
import org.pmw.tinylog.Logger;

public class DisconnectImpl implements MqttAction {

    private static DisconnectImpl instance = null;
    private Disconnect param;

    private DisconnectImpl(Disconnect disconnect) {
        this.param = disconnect;
    }

    public static DisconnectImpl get(final Disconnect param) {
        if (instance == null) {
            instance = new DisconnectImpl(param);
        }
        return instance;
    }


    @Override
    public void run() {
        if (param.isDebug()) {
            Logger.debug("Command: {} ", param);
        }

        try {
            MqttClientUtils.getInstance().disconnect(param);
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
