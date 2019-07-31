package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.PublishImpl;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import picocli.CommandLine;

import java.util.Arrays;

@CommandLine.Command(name = "pub", description = "Publish a message to a list of topics")
public class Publish extends Connect implements MqttAction {

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The Topic, at least one.")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-m", "--message"}, required = true, description = "The message that should be published.")
    private String message;

    @CommandLine.Option(names = {"-r", "--retain"}, defaultValue = "false", description = "The message will be retained.")
    private boolean retain;

    public String[] getTopics() {
        return topics;
    }

    public MqttQos[] getQos() {
        return qos;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetain() {
        return retain;
    }

    @Override
    public Class getType() {
        return Publish.class;
    }

    @Override
    public void run() {
        PublishImpl.get(this).run();
    }

    @Override
    public String toString() {
        return "Publish:: {" +
                "key=" + getKey() +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", retain=" + retain +
                '}';
    }

}
