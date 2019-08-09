package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import org.pmw.tinylog.Logger;

public class MqttClientExecutor extends AbstractMqttClientExecutor {

    private static MqttClientExecutor instance = null;

    private MqttClientExecutor() {
    }

    public static MqttClientExecutor getInstance() {
        if (instance == null) {
            instance = new MqttClientExecutor();
        }
        return instance;
    }

    boolean mqttConnect(final @NotNull Mqtt5BlockingClient client, Mqtt5Connect connectMessage, final @NotNull Connect connectCommand) {
        Mqtt5ConnAck connAck = client.connect(connectMessage);
        if (connectCommand.isDebug()) {
            Log.debug("Client connect with {} ", connectCommand.toString());
        } else {
            Logger.info("Client connect with {} ", connAck.getReasonCode());
        }
        return client.getConfig().getState().isConnected();
    }

    void mqttSubscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final String topic, final MqttQos qos) {
        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {
                    final String p = new String(publish.getPayloadAsBytes());
                    if (subscribe.isDebug()) {
                        Log.debug("Client received on topic: {} message: '{}' ", topic, p);
                    } else {
                        Logger.info("Client received msg: '{}...' ", p.length() > 10 ? p.substring(0, 10) : p);
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribe.isDebug()) {
                            Log.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }

}
