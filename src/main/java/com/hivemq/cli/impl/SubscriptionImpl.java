package com.hivemq.cli.impl;

import com.hivemq.cli.commands.HmqSub;
import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;

import javax.inject.Inject;
import java.util.List;

public class SubscriptionImpl implements MqttAction {

    private HmqSub param;
    private Mqtt3Client mqttClient;

    @Inject
    SubscriptionImpl(final HmqSub param, final Mqtt3Client mqttClient) {
        this.param = param;
        this.mqttClient = mqttClient;
    }

    @Override
    public void run() {

        Mqtt3BlockingClient mqtt3BlockingClient = mqttClient.toBlocking();

        try {

            Mqtt3ConnAck connAck = mqtt3BlockingClient.connect();

            List<Mqtt3SubAckReturnCode> returnCodes = mqtt3BlockingClient.subscribe(Mqtt3Subscribe.builder().topicFilter("#").build()).getReturnCodes();

        } catch (Mqtt3ConnAckException ex) {
            System.out.println("Miep");
        }
        catch (Mqtt3SubAckException ex) {
            System.out.println(ex.getMqttMessage().getReturnCodes());
            System.out.println("Miep");
        }

//        System.out.println(connAck.getReturnCode());
        System.out.printf("Sub");
    }
}
