package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.util.MqttClientUtils;
import org.pmw.tinylog.Logger;

public class PublishImpl implements MqttAction {

    private static PublishImpl instance = null;
    private Publish param;

    private PublishImpl(Publish publish) {
        this.param = publish;
    }

    public static PublishImpl get(final Publish param) {
        if (instance == null) {
            instance = new PublishImpl(param);
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
            MqttClientUtils.getInstance().publish(param);
        } catch (Exception ex) {
            if (param.isDebug()) {
                Logger.error(ex);
            } else {
                Logger.error(ex.getMessage());
            }
        }
    }

}
