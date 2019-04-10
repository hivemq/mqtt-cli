package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.internal.mqtt.MqttBlockingClient;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;

import javax.inject.Inject;
import java.util.List;

public class SubscriptionImpl implements MqttAction {

    private Subscribe param;
    private Mqtt5BlockingClient mqttBlockingClient;

    @Inject
    SubscriptionImpl(final Subscribe param, final Mqtt5Client mqttClient) {
        this.param = param;
        this.mqttBlockingClient = mqttClient.toBlocking();
    }

    @Override
    public void run() {
        try {
            Mqtt5ConnAck connAck = mqttBlockingClient.connect();
            System.out.println("Connect: " + mqttBlockingClient.getConfig().getClientIdentifier().get() );
            for(int i=0; i < param.getTopics().length; i++) {
                final String topic = param.getTopics()[i];
                final MqttQos qos = getQosFromParam(param.getQos(), i);

                List<Mqtt5SubAckReasonCode> returnCodes =
                        mqttBlockingClient
                                .subscribeWith()
                                .topicFilter(topic)
                                .qos(qos)
                                .send().getReasonCodes();

                System.out.println("Subscribed to Topic: " + topic + " and " + returnCodes);

            }

        } catch (Mqtt5ConnAckException ex) {
            System.err.println(ex.getMqttMessage());
        } catch (Mqtt5SubAckException e) {
            System.err.println(e.getMqttMessage().getReasonCodes());
        }

    }

    private MqttQos getQosFromParam(int[] qos, int i) {
        if( qos.length < i || qos[i] == 0) {
            return MqttQos.AT_MOST_ONCE;
        }
        return qos[i]==1 ? MqttQos.AT_LEAST_ONCE:MqttQos.EXACTLY_ONCE;
    }

}
