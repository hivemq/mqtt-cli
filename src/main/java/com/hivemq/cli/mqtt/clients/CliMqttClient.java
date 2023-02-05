package com.hivemq.cli.mqtt.clients;

import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.listeners.LogDisconnectedListener;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public interface CliMqttClient {

    static @NotNull ConnectBuilder connectWith(final @NotNull ConnectOptions connectOptions) {
        return new ConnectBuilder(connectOptions)
                .addDisconnectedListener(LogDisconnectedListener.INSTANCE);
    }

    void publish(final @NotNull PublishOptions publishOptions);

    void subscribe(final @NotNull SubscribeOptions subscribeOptions);

    void unsubscribe(final @NotNull UnsubscribeOptions unsubscribeOptions);

    void disconnect(final @NotNull DisconnectOptions disconnectOptions);

    boolean isConnected();

    @NotNull String getClientIdentifier();

    @NotNull String getServerHost();

    @NotNull MqttVersion getMqttVersion();

    @NotNull LocalDateTime getConnectedTime();

    @NotNull MqttClientState getState();

    @NotNull String getSslProtocols();

    int getServerPort();

    @NotNull List<MqttTopicFilter> getSubscribedTopics();
}
