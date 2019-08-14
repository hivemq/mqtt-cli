package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TestableMqttClientExecutor extends AbstractMqttClientExecutor {

    private static TestableMqttClientExecutor instance = null;
    private Mqtt5Connect connectMgs;
    private ArrayList<String> subscribeTopic;

    private TestableMqttClientExecutor() {
    }

    public static TestableMqttClientExecutor getInstance() {
        if (instance == null) {
            instance = new TestableMqttClientExecutor();
        }
        return instance;
    }

    public Mqtt5Connect getConnectMgs() {
        return connectMgs;
    }

    public ArrayList<String> getSubscribeTopic() {
        return subscribeTopic;
    }

    @Override
    boolean mqttConnect(@NotNull Mqtt5BlockingClient client, Mqtt5Connect connectMessage, ConnectCommand connectCommand) {

        connectMgs = connectMessage;
        subscribeTopic = new ArrayList<>();
        return true;
    }

    @Override
    void mqttSubscribe(Mqtt5AsyncClient client, @NotNull SubscribeCommand subscribeCommand, String topic, MqttQos qos) {
        subscribeTopic.add(topic);
    }


}
