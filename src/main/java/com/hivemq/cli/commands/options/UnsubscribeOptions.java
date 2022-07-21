package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;


public class UnsubscribeOptions {

    public UnsubscribeOptions() {
    }

    public UnsubscribeOptions(final @NotNull String @NotNull [] topics, final @Nullable Mqtt5UserProperty @Nullable [] userProperties) {
        this.topics = topics;
        this.userProperties = userProperties;
    }

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property for the unsubscribe message")
    private @NotNull Mqtt5UserProperty @Nullable [] userProperties;

    public @NotNull String[] getTopics() {
        return topics;
    }

    public @NotNull Mqtt5UserProperties getUserProperties() {
        if (userProperties != null && userProperties.length > 0) {
            return Mqtt5UserProperties.of(userProperties);
        } else {
            return Mqtt5UserProperties.of();
        }
    }

    public static @NotNull UnsubscribeOptions of(final @NotNull SubscribeOptions subscribeOptions) {
        return new UnsubscribeOptions(subscribeOptions.getTopics(), subscribeOptions.getUserPropertiesRaw());
    }

    public void logUnusedUnsubscribeOptions(final @NotNull MqttVersion mqttVersion) {
        if (mqttVersion == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn(
                        "Unsubscribe user properties were set but are unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }
}
