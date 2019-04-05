package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

public class MqttClientFactory implements Provider<Mqtt3Client> {

    private AbstractCommand command;

    @Inject
    public MqttClientFactory(final AbstractCommand command) {
        this.command = command;
    }

    @Override
    public Mqtt3Client get() {

        Mqtt3ClientBuilder builder = MqttClient.builder().useMqttVersion3()
                .serverHost(command.getHost())
                .serverPort(command.getPort());

        if (command.getIdentifier().isPresent()) {
            builder.identifier(MqttClientIdentifier.of(command.getIdentifier().get()));
        }

        return builder.build();
    }
}
