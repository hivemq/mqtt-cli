package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.commands.cli.ConnectCommand;
import com.hivemq.cli.commands.Publish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TestableMqttClientExecutor extends AbstractMqttClientExecutor {

    private static TestableMqttClientExecutor instance = null;
    private Mqtt5Connect connectMsg;
    private ArrayList<String> subscribeTopic;

    private TestableMqttClientExecutor() {
    }

    public static TestableMqttClientExecutor getInstance() {
        if (instance == null) {
            instance = new TestableMqttClientExecutor();
        }
        return instance;
    }

    public Mqtt5Connect getConnectMsg() {
        return connectMsg;
    }

    public ArrayList<String> getSubscribeTopic() {
        return subscribeTopic;
    }


    @Override
    void mqtt5Connect(@NotNull Mqtt5BlockingClient client, @NotNull Mqtt5Connect connectMessage, @NotNull ConnectCommand connectCommand) {

    }

    @Override
    void mqtt3Connect(@NotNull Mqtt3BlockingClient client, @NotNull Mqtt3Connect connectMessage, @NotNull ConnectCommand connectCommand) {

    }

    @Override
    void mqtt5Subscribe(@NotNull Mqtt5AsyncClient client, @NotNull Subscribe subscribe, @NotNull String topic, @NotNull MqttQos qos) {

    }

    @Override
    void mqtt3Subscribe(@NotNull Mqtt3AsyncClient client, @NotNull Subscribe subscribe, @NotNull String topic, @NotNull MqttQos qos) {

    }

    @Override
    void mqtt5Publish(@NotNull Mqtt5AsyncClient client, @NotNull Publish publish, @NotNull String topic, @NotNull MqttQos qos) {

    }

    @Override
    void mqtt3Publish(@NotNull Mqtt3AsyncClient client, @NotNull Publish publish, @NotNull String topic, @NotNull MqttQos qos) {

    }

    @Override
    void mqtt5Unsubscribe(@NotNull Mqtt5Client client, @NotNull Unsubscribe unsubscribe) {

    }

    @Override
    void mqtt3Unsubscribe(@NotNull Mqtt3Client client, @NotNull Unsubscribe unsubscribe) {

    }

    @Override
    void mqtt5Disconnect(@NotNull Mqtt5Client client, @NotNull Disconnect disconnect) {

    }

    @Override
    void mqtt3Disconnect(@NotNull Mqtt3Client client, @NotNull Disconnect disconnect) {

    }
}
