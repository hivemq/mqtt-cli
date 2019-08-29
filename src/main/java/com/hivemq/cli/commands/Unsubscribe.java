package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;

public interface Unsubscribe extends Context {

    String[] getTopics();

    Mqtt5UserProperties getUserProperties();
}
