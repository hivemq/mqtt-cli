package com.hivemq.cli.mqtt.clients.mqtt3;

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.WillOptions;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt3PublishCallback;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CliMqtt3ClientFactory {

    public static @NotNull CliMqtt3Client create(
            final @NotNull ConnectOptions connectOptions,
            final @Nullable SubscribeOptions subscribeOptions,
            final @NotNull List<MqttClientDisconnectedListener> disconnectedListeners) throws Exception {
        final Mqtt3Client client = buildClient(connectOptions, disconnectedListeners);

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING,
                        buildRemainingMqtt3PublishesCallback(subscribeOptions, client));

        return new CliMqtt3Client(client);
    }

    private static @NotNull Mqtt3Client buildClient(
            final @NotNull ConnectOptions connectOptions,
            final @NotNull List<MqttClientDisconnectedListener> disconnectedListeners) throws Exception {
        final Mqtt3ClientBuilder clientBuilder = Mqtt3Client.builder()
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

    private static @Nullable Mqtt3Publish buildWill(final @NotNull WillOptions willOptions) {
        if (willOptions.getWillTopic() != null) {
            final ByteBuffer willPayload = willOptions.getWillMessage();
            final Mqtt3PublishBuilder.Complete builder = Mqtt3Publish.builder()
                    .topic(willOptions.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(willOptions.getWillQos()));

            if (willOptions.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(willOptions.getWillRetain());
            }
            return builder.build();
        } else if (willOptions.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - will options were: {} ",
                    willOptions.toString());
        }
        return null;
    }

    private static @NotNull Consumer<Mqtt3Publish> buildRemainingMqtt3PublishesCallback(
            final @Nullable SubscribeOptions subscribeOptions, final @NotNull Mqtt3Client client) {
        if (subscribeOptions != null) {
            return new SubscribeMqtt3PublishCallback(subscribeOptions, client);
        } else {
            return mqtt3Publish -> Logger.debug("received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt3Publish,
                    new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }
}
