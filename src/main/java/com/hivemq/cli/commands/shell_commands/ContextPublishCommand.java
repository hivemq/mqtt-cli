package com.hivemq.cli.commands.shell_commands;

import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.converters.*;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Arrays;


@CommandLine.Command(name = "pub",
        aliases = "publish",
        description = "Publish a message to a list of topics",
        abbreviateSynopsis = false)

public class ContextPublishCommand extends ShellContextCommand implements Runnable, Publish {

    @Inject
    public ContextPublishCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, converter = MqttQosConverter.class, defaultValue = "0", description = "Quality of service for the corresponding topic (default for all: 0)")
    private MqttQos[] qos;

    @CommandLine.Option(names = {"-m", "--message"}, converter = ByteBufferConverter.class, required = true, description = "The message to publish")
    private ByteBuffer message;

    @CommandLine.Option(names = {"-r", "--retain"}, defaultValue = "false", description = "The message will be retained (default: false)")
    private boolean retain;

    @CommandLine.Option(names = {"-pe", "--messageExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the publish message in seconds (default: no message expiry)")
    @Nullable
    private Long messageExpiryInterval;

    @CommandLine.Option(names = {"-pf", "--payloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class, description = "The payload format indicator of the publish message")
    @Nullable
    private Mqtt5PayloadFormatIndicator payloadFormatIndicator;

    @CommandLine.Option(names = {"-pc", "--contentType"}, description = "A description of publish message's content")
    @Nullable
    private String contentType;

    @CommandLine.Option(names = {"-pr", "--responseTopic"}, description = "The topic name for the publish message`s response message")
    @Nullable
    private String responseTopic;

    @CommandLine.Option(names = {"-pd", "--correlationData"}, converter = ByteBufferConverter.class, description = "The correlation data of the publish message")
    @Nullable
    private ByteBuffer correlationData;

    @CommandLine.Option(names = {"-pu", "--publishUserProperties"}, converter = UserPropertiesConverter.class, description = "The user property of the publish message (usage: 'Key=Value', 'Key1=Value1|Key2=Value2)'")
    @Nullable
    private Mqtt5UserProperties publishUserProperties;


    @Override
    public void run() {
        logUnusedPublishOptions();

        if (isVerbose()) {
            Logger.trace("Command {} ", this);
        }

        try {
            mqttClientExecutor.publish(contextClient, this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
        }
    }

    private void logUnusedPublishOptions() {
        if (contextClient.getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (messageExpiryInterval != null) {
                Logger.warn("Publish message expiry was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (payloadFormatIndicator != null) {
                Logger.warn("Publish payload format indicator was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (contentType != null) {
                Logger.warn("Publish content type was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (responseTopic != null) {
                Logger.warn("Publish response topic was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (correlationData != null) {
                Logger.warn("Publish correlation data was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
            if (publishUserProperties != null) {
                Logger.warn("Publish user properties were set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Override
    public String toString() {
        return "ContextPublish:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                ", retain=" + retain +
                ", messageExpiryInterval=" + messageExpiryInterval +
                ", payloadFormatIndicator=" + payloadFormatIndicator +
                ", contentType=" + contentType +
                ", responseTopic=" + responseTopic +
                ", correlationData=" + correlationData +
                ", userProperties=" + publishUserProperties +
                '}';
    }

    @Override
    public Class getType() {
        return ContextPublishCommand.class;
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

    public Long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    public void setMessageExpiryInterval(@Nullable final Long messageExpiryInterval) {
        this.messageExpiryInterval = messageExpiryInterval;
    }

    @Nullable
    public Mqtt5PayloadFormatIndicator getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public void setPayloadFormatIndicator(@Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    @Nullable
    public String getContentType() {
        return contentType;
    }

    public void setContentType(@Nullable final String contentType) {
        this.contentType = contentType;
    }

    @Nullable
    public String getResponseTopic() {
        return responseTopic;
    }

    public void setResponseTopic(@Nullable final String responseTopic) {
        this.responseTopic = responseTopic;
    }

    @Nullable
    public ByteBuffer getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(@Nullable final ByteBuffer correlationData) {
        this.correlationData = correlationData;
    }

    @Nullable
    public Mqtt5UserProperties getPublishUserProperties() {
        return publishUserProperties;
    }

    public void setPublishUserProperties(@Nullable final Mqtt5UserProperties publishUserProperties) {
        this.publishUserProperties = publishUserProperties;
    }
}
