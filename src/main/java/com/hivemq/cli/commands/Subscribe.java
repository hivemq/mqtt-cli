package com.hivemq.cli.commands;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import com.hivemq.cli.ioc.MqttClientModule;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "sub", description = "Subscribe an mqtt client to a list of topics")
public class Subscribe extends Connect implements MqttAction {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "Set at least one Topic")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private int[] qos;

    public String[] getTopics() {
        return topics;
    }

    public int[] getQos() {
        return qos;
    }

    @Override
    public Class getType() {
        return Subscribe.class;
    }

    @Override
    public void run() {
        HmqCli.executeCommand(this);
    }
}
