package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import picocli.CommandLine;

import java.util.Arrays;

@CommandLine.Command(name = "sub", description = "Subscribe an mqtt client to a list of topics")
public class Subscribe extends Connect implements MqttAction {


    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "Set at least one Topic")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private MqttQos[] qos;

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(MqttQos[] qos) {
        this.qos = qos;
    }


    @Override
    public Class getType() {
        return Subscribe.class;
    }

    @Override
    public void run() {
        SubscriptionImpl.get(this).run();
    }

    public void stay() throws InterruptedException {
        SubscriptionImpl.get(this).stay();
    }

    @Override
    public String toString() {
        return "Subscribe:: {" +
                "key=" + getKey() +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                '}';
    }


}
