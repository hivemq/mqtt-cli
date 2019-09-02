package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;

@CommandLine.Command(name = "unsub",
        aliases = "unsubscribe",
        description = "Unsubscribes this mqtt client from a list of topics")

public class ContextUnsubscribeCommand extends ShellContextCommand implements Runnable, Unsubscribe {


    @Inject
    public ContextUnsubscribeCommand(@NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private String[] topics;

    @CommandLine.Option(names = {"-u", "--userProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the unsubscribe message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')")
    @Nullable
    private Mqtt5UserProperties userProperties;


    @Override
    public void run() {
        if (isVerbose()) {
            Logger.trace("Command {} ", this);
        }

        try {
            mqttClientExecutor.unsubscribe(contextClient, this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ContextUnsubscribe:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", userProperties=" + userProperties +
                '}';
    }

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    public Mqtt5UserProperties getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(final Mqtt5UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
