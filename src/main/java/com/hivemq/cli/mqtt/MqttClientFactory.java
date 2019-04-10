package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

public class MqttClientFactory implements Provider<Mqtt5Client> {

    private AbstractCommand command;

    @Inject
    public MqttClientFactory(final AbstractCommand command) {
        this.command = command;
    }

    @Override
    public Mqtt5Client get() {

        MqttClientBuilder mqttClientBuilder = MqttClient.builder()
                .serverHost(command.getHost())
                .serverPort(command.getPort())
                .identifier(command.getIdentifier());
        return mqttClientBuilder.useMqttVersion5().build();
    }
}
