package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.util.MqttUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;

import java.util.List;

public class SubscriptionImpl implements MqttAction {

    private Subscribe param;

    public SubscriptionImpl(final Subscribe param) {
        this.param = param;
    }

    @Override
    public void run() {
        // System.out.println(param);

        try {
            Mqtt5BlockingClient mqttBlockingClient = MqttUtils.connect((Connect) param);
            if (mqttBlockingClient.getConfig().getState().isConnected()) {
                for (int i = 0; i < param.getTopics().length; i++) {
                    final String topic = param.getTopics()[i];
                    final MqttQos qos = getQosFromParam(param.getQos(), i);
                    List<Mqtt5SubAckReasonCode> returnCodes =
                            (mqttBlockingClient).subscribeWith()
                                    .topicFilter(topic)
                                    .qos(qos)
                                    .send().getReasonCodes();

                    System.out.println("Subscribed to Topic: " + topic + " with result: " + returnCodes);
                }
            }
        } catch (Mqtt5SubAckException e) {
            System.err.println(e.getMqttMessage().getReasonCodes());
        }
    }


    private MqttQos getQosFromParam(int[] qos, int i) {
        if (qos.length < i || qos[i] == 0) {
            return MqttQos.AT_MOST_ONCE;
        }
        return qos[i] == 1 ? MqttQos.AT_LEAST_ONCE : MqttQos.EXACTLY_ONCE;
    }

}
