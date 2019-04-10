package com.hivemq.cli.ioc;

import com.google.inject.AbstractModule;
import com.hivemq.cli.mqtt.MqttClientFactory;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

public class MqttClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Mqtt5Client.class).toProvider(MqttClientFactory.class);
        super.configure();
    }
}
