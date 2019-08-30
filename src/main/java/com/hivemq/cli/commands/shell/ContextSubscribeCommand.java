package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.UserInterruptException;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

@CommandLine.Command(name = "sub",
        aliases = "subscribe",
        description = "Subscribe this mqtt client to a list of topics")
public class ContextSubscribeCommand extends ShellContextCommand implements Runnable, Subscribe {

    public static final int IDLE_TIME = 5000;

    @Inject
    public ContextSubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to subscribe to")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of service for the corresponding topics (default for all: 0)")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-sup", "--subscribeUserProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the subscribe message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')")
    @Nullable Mqtt5UserProperties subscribeUserProperties;

    @CommandLine.Option(names = {"-of", "--outputToFile"}, description = "A file to which the received publish messages will be written")
    @Nullable
    private File receivedMessagesFile;

    @CommandLine.Option(names = {"-oc", "--outputToConsole"}, defaultValue = "false", description = "The received messages will be written to the console (default: false)")
    private boolean printToSTDOUT;

    @CommandLine.Option(names = {"-s", "--stay"}, defaultValue = "false", description = "The subscribe will block the console and wait for publish messages to print (default: false)")
    private boolean stay;

    @CommandLine.Option(names = {"-b64", "--base64"}, description = "Specify the encoding of the received messages as Base64 (default: false)")
    private boolean base64;

    @Override
    public void run() {


        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);
            mqttClientExecutor.subscribe(contextClient, this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.error(ex);
            }
            Logger.error(ex.getMessage());
        }

        if (stay) {
            final boolean consoleOutputBefore = printToSTDOUT;
            printToSTDOUT = true;
            try {
                stay();
            } catch (final InterruptedException e) {
                if (isDebug()) {
                    Logger.debug(e);
                }
                Logger.error(e.getMessage());
            } finally {
                printToSTDOUT = consoleOutputBefore;
            }
        }


    }

    private void stay() throws InterruptedException {

        while (mqttClientExecutor.isConnected(this)) {
            Thread.sleep(IDLE_TIME);
        }
        if (isVerbose()) {
            Logger.trace("Client disconnected.");
        }

    }

    @Override
    public String toString() {
        return "ContextSubscribe:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", userProperties=" + subscribeUserProperties +
                ", toFile=" + receivedMessagesFile +
                ", outputToConsole=" + printToSTDOUT +
                ", base64=" + base64 +
                '}';
    }

    @Override
    public Class getType() {
        return ContextSubscribeCommand.class;
    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    public MqttQos[] getQos() {
        return qos;
    }

    public void setQos(final MqttQos[] qos) {
        this.qos = qos;
    }

    public File getReceivedMessagesFile() {
        return receivedMessagesFile;
    }

    public void setReceivedMessagesFile(@Nullable final File receivedMessagesFile) {
        this.receivedMessagesFile = receivedMessagesFile;
    }

    public boolean isPrintToSTDOUT() {
        return printToSTDOUT;
    }

    public void setPrintToSTDOUT(final boolean printToSTDOUT) {
        this.printToSTDOUT = printToSTDOUT;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(final boolean base64) {
        this.base64 = base64;
    }


    @Override
    @Nullable
    public Mqtt5UserProperties getSubscribeUserProperties() {
        return subscribeUserProperties;
    }

    @Override
    public void setSubscribeUserProperties(@Nullable final Mqtt5UserProperties subscribeUserProperties) {
        this.subscribeUserProperties = subscribeUserProperties;
    }
}
