package com.hivemq.cli.commands;

import com.google.common.annotations.VisibleForTesting;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

@CommandLine.Command(name = "sub", description = "Subscribe an mqtt client to a list of topics")
public class SubscribeCommand extends ConnectCommand implements MqttAction {


    @Inject
    public SubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        super(mqttClientExecutor);

    }


    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "Set at least one Topic")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "The file to which the received messages will be written.")
    private File receivedMessagesFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false", description = "The received messages will be written to the console.")
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64")
    private boolean base64;

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

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(boolean base64) {
        this.base64 = base64;
    }

    @Override
    public Class getType() {
        return SubscribeCommand.class;
    }

    @Override
    public void run() {

        if (isDebug()) {
            Logger.debug("Command: {} ", this);
        }

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(this);
        } catch (Exception ex) {
            if (isDebug()) {
                Logger.error(ex);
            } else {
                Logger.error(ex.getMessage());
            }
        }

        if (!ShellCommand.IN_SHELL) {
            if (receivedMessagesFile == null && !printToSTDOUT) {
                printToSTDOUT = true;
            }
            try {
                stay();
            } catch (InterruptedException e) {
                Logger.error(e.getMessage());
            }
        }

    }

    private void stay() throws InterruptedException {
        synchronized (this) {
            while (mqttClientExecutor.isConnected(this)) {
                this.wait(500);
            }
            Logger.debug("Client disconnected.");
        }
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
