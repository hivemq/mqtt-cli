package com.hivemq.cli.commands;

import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.impl.MqttAction;
import picocli.CommandLine;

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
