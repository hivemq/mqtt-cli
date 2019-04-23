package com.hivemq.cli.util;

import com.hivemq.cli.commands.Connect;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class MqttUtils {
    private static MqttClientBuilder build(Connect connect) {
        return MqttClient.builder()
                .serverHost(connect.getHost())
                .serverPort(connect.getPort())
                .identifier(connect.getIdentifier());
    }

    public static Mqtt5BlockingClient connect(Connect connect) {
        MqttClientBuilder mqttClientBuilder = build(connect);
        Mqtt5BlockingClient mqttBlockingClient = mqttClientBuilder.useMqttVersion5().build().toBlocking();
        Mqtt5ConnAck connAck = null;
        try {
            System.out.print("Connect ");
            connAck = mqttBlockingClient.connect();
            System.out.println("Connected: " + mqttBlockingClient.getConfig().getClientIdentifier().get());

        } catch (Mqtt5ConnAckException ex) {
            if( null != connAck) {
                System.err.println(connAck.getReasonCode() + "  " + connAck.getReasonString().get());
            } else {
                System.err.println(ex.getCause().getMessage());
            }
        }
        return mqttBlockingClient;
    }
}
