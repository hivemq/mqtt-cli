package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import picocli.CommandLine;

import java.io.File;
import java.util.Arrays;

@CommandLine.Command(name = "sub", description = "Subscribe an mqtt client to a list of topics")
public class Subscribe extends Connect implements MqttAction {

    private boolean printToSTDOUT = false;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "Set at least one Topic")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The file to which the received messages will be written.")
    private File receivedMessagesFile;

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

    public File getReceivedMessagesFile() {
        return receivedMessagesFile;
    }

    public void setReceivedMessagesFile(File receivedMessagesFile) {
        this.receivedMessagesFile = receivedMessagesFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public void setPrintToSTDOUT(boolean printToSTDOUT) {
        this.printToSTDOUT = printToSTDOUT;
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
                ", toFile=" + receivedMessagesFile +
                '}';
    }


}
