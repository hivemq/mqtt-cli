package com.hivemq.cli.mqtt.clients.mqtt5;

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.WillOptions;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt5PublishCallback;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CliMqtt5ClientFactory {

    public static @NotNull CliMqtt5Client create(
            final @NotNull ConnectOptions connectOptions,
            final @Nullable SubscribeOptions subscribeOptions,
            final @NotNull List<MqttClientDisconnectedListener> disconnectedListeners) throws Exception {
        final Mqtt5Client client = buildClient(connectOptions, disconnectedListeners);

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING,
                        buildRemainingMqtt5PublishesCallback(subscribeOptions, client));

        return new CliMqtt5Client(client);
    }

    private static @NotNull Mqtt5Client buildClient(
            final @NotNull ConnectOptions connectOptions,
            final @NotNull List<MqttClientDisconnectedListener> disconnectedListeners) throws Exception {
        final Mqtt5ClientBuilder clientBuilder = Mqtt5Client.builder()
                .webSocketConfig(connectOptions.getWebSocketConfig())
                .serverHost(connectOptions.getHost())
                .serverPort(connectOptions.getPort())
                .sslConfig(connectOptions.buildSslConfig());

        for (final MqttClientDisconnectedListener disconnectedListener : disconnectedListeners) {
            //noinspection ResultOfMethodCallIgnored
            clientBuilder.addDisconnectedListener(disconnectedListener);
        }

        if (connectOptions.getIdentifier() != null) {
            //noinspection ResultOfMethodCallIgnored
            clientBuilder.identifier(connectOptions.getIdentifier());
        }

        //noinspection ResultOfMethodCallIgnored
        clientBuilder.willPublish(buildWill(connectOptions.getWillOptions()));

        return clientBuilder.build();
    }

    private static @Nullable Mqtt5WillPublish buildWill(final @NotNull WillOptions willOptions) {
        // only topic is mandatory for will message creation
        if (willOptions.getWillTopic() != null) {
            final ByteBuffer willPayload = willOptions.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(willOptions.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(willOptions.getWillQos()))
                    .payloadFormatIndicator(willOptions.getWillPayloadFormatIndicator())
                    .contentType(willOptions.getWillContentType())
                    .responseTopic(willOptions.getWillResponseTopic())
                    .correlationData(willOptions.getWillCorrelationData());

            if (willOptions.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(willOptions.getWillRetain());
            }
            if (willOptions.getWillMessageExpiryInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.messageExpiryInterval(willOptions.getWillMessageExpiryInterval());
            }
            if (willOptions.getWillDelayInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.delayInterval(willOptions.getWillDelayInterval());
            }
            if (willOptions.getWillUserProperties() != null) { // user Properties can't be completed with null
                //noinspection ResultOfMethodCallIgnored
                builder.userProperties(willOptions.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (willOptions.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - will options were: {} ",
                    willOptions.toString());
            return null;
        } else {
            return null;
        }
    }

    private static @NotNull Consumer<Mqtt5Publish> buildRemainingMqtt5PublishesCallback(
            final @Nullable SubscribeOptions subscribeOptions, final @NotNull Mqtt5Client client) {
        if (subscribeOptions != null) {
            return new SubscribeMqtt5PublishCallback(subscribeOptions, client);
        } else {
            return mqtt5Publish -> Logger.debug("received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt5Publish,
                    new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }

}
