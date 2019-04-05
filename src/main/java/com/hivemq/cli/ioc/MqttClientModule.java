package com.hivemq.cli.ioc;

import com.google.inject.AbstractModule;
import com.hivemq.cli.mqtt.MqttClientFactory;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

public class MqttClientModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Mqtt3Client.class).toProvider(MqttClientFactory.class);
        super.configure();
    }
}
