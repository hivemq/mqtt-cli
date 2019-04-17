package com.hivemq.cli.impl;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class SubscriptionImpl implements MqttAction {

    private Subscribe param;

    public SubscriptionImpl(final Subscribe param) {
        this.param = param;
    }

    @Override
    public void run() {

        System.out.println(param);

        MqttClientBuilder mqttClientBuilder = MqttClient.builder()
                .serverHost(param.getHost())
                .serverPort(param.getPort())
                .identifier(param.getIdentifier());
        try {
        Mqtt5Client mqttBlockingClient = mqttClientBuilder.useMqttVersion5().build().toBlocking();


            Mqtt5ConnAck connAck = ((Mqtt5BlockingClient) mqttBlockingClient).connect();

            System.out.println("Connect: " + mqttBlockingClient.getConfig().getClientIdentifier().get());

            /**
             try {
             Mqtt5ConnAck connAck = mqttBlockingClient.connect();
             System.out.println("Connect: " + mqttBlockingClient.getConfig().getClientIdentifier().get());
             for (int i = 0; i < param.getTopics().length; i++) {
             final String topic = param.getTopics()[i];
             final MqttQos qos = getQosFromParam(param.getQos(), i);
             List<Mqtt5SubAckReasonCode> returnCodes =
             mqttBlockingClient
             .subscribeWith()
             .topicFilter(topic)
             .qos(qos)
             .send().getReasonCodes();

             System.out.println("Subscribed to Topic: " + topic + " and " + returnCodes);
             */

             } catch (
    Mqtt5ConnAckException ex) {
             System.err.println(ex.getMqttMessage());
             } catch (
    Mqtt5SubAckException e) {
             System.err.println(e.getMqttMessage().getReasonCodes());
             }
    }

    /*
    private MqttQos getQosFromParam(int[] qos, int i) {
        if (qos.length < i || qos[i] == 0) {
            return MqttQos.AT_MOST_ONCE;
        }
        return qos[i] == 1 ? MqttQos.AT_LEAST_ONCE : MqttQos.EXACTLY_ONCE;
    }
   */
}
