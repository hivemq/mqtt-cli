package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.util.MqttUtils;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;

public class ConnectionImpl implements MqttAction {

    private Connect param;

    public ConnectionImpl(final Connect param) {
        this.param = param;
    }

    @Override
    public void run() {
        try {
            MqttUtils.connect(param);
        } catch (Mqtt5ConnAckException ex) {
            System.err.println(ex.getMqttMessage());
        } catch (Mqtt5SubAckException e) {
            System.err.println(e.getMqttMessage().getReasonCodes());
        }
    }

}
