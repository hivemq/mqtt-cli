package com.hivemq.cli.commands;

import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;

@CommandLine.Command(name = "pub", aliases = "publish", description = "Publish a message to a list of topics")
public class PublishCommand extends ConnectCommand implements MqttAction {


    @Inject
    public PublishCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        super(mqttClientExecutor);

    }

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The Topic, at least one.")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-m", "--message"}, converter = ByteBufferConverter.class, required = true, description = "The message that should be published.")
    private ByteBuffer message;

    @CommandLine.Option(names = {"-r", "--retain"}, defaultValue = "false", description = "The message will be retained.")
    private boolean retain;

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

    public ByteBuffer getMessage() {
        return message;
    }

    public void setMessage(final ByteBuffer message) {
        this.message = message;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(final boolean retain) {
        this.retain = retain;
    }

    @Override
    public Class getType() {
        return PublishCommand.class;
    }

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        handleConnectOptions();

        try {
            mqttClientExecutor.publish(this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
        }

    }

    @Override
    public String toString() {
        return "Publish:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", retain=" + retain +
                ", connectOptions: {" + connectOptions() + "}" +
                '}';
    }

}
