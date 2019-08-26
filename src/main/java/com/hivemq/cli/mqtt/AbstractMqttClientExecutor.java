package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.DisconnectCommand;
import com.hivemq.cli.commands.PublishCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMqttClientExecutor {

    private final ClientCache<String, MqttClient> clientCache = new ClientCache<>();
    private final Map<String, LocalDateTime> clientCreationTimes = new HashMap<>();


    abstract void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos);


    public void subscribe(final @NotNull SubscribeCommand subscribeCommand) {

        final MqttClient client = getMqttClientFromCacheOrConnect(subscribeCommand);

        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());


        for (int i = 0; i < subscribeCommand.getTopics().length; i++) {
            final String topic = subscribeCommand.getTopics()[i];
            final MqttQos qos = subscribeCommand.getQos()[i];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Subscribe((Mqtt5AsyncClient) client, subscribeCommand, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Subscribe((Mqtt3AsyncClient) client, subscribeCommand, topic, qos);
                    break;
            }
        }

    }

    public void publish(final @NotNull PublishCommand publishCommand) {

        final MqttClient client = getMqttClientFromCacheOrConnect(publishCommand);

        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < publishCommand.getTopics().length; i++) {
            final String topic = publishCommand.getTopics()[i];
            final MqttQos qos = publishCommand.getQos()[i];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Publish((Mqtt5AsyncClient) client, publishCommand, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Publish((Mqtt3AsyncClient) client, publishCommand, topic, qos);
                    break;
            }

        }
    }

    public void disconnect(final @NotNull DisconnectCommand disconnectCommand) {

        LoggingContext.put("identifier", disconnectCommand.getIdentifier());

        clientCache.setVerbose(disconnectCommand.isVerbose());

        if (clientCache.hasKey(disconnectCommand.getKey())) {
            final MqttClient client = clientCache.get(disconnectCommand.getKey());
            clientCache.remove(disconnectCommand.getKey());

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    ((Mqtt5AsyncClient) client).disconnect();
                    break;
                case MQTT_3_1_1:
                    ((Mqtt3AsyncClient) client).disconnect();
                    break;
            }
        } else if (disconnectCommand.isDebug()) {
            Logger.debug("client to disconnect is not connected: {} ", disconnectCommand.getKey());
        }

    }

    public boolean isConnected(final @NotNull SubscribeCommand subscriber) {

        LoggingContext.put("identifier", subscriber.getIdentifier());

        clientCache.setVerbose(subscriber.isVerbose());

        if (clientCache.hasKey(subscriber.getKey())) {
            final MqttClient client = clientCache.get(subscriber.getKey());
            final MqttClientState state = client.getState();
            if (subscriber.isVerbose()) {
                Logger.trace("in State: {}", state);
            }
            return state.isConnected();
        }
        return false;
    }


    public @NotNull MqttClient connect(final @NotNull ConnectCommand connectCommand) {

        final String identifier = connectCommand.createIdentifier();

        LoggingContext.put("identifier", identifier);

        clientCache.setVerbose(connectCommand.isVerbose());

        switch (connectCommand.getVersion()) {
            case MQTT_5_0:
                return connectMqtt5Client(connectCommand, identifier);
            case MQTT_3_1_1:
                return connectMqtt3Client(connectCommand, identifier);
        }

        throw new IllegalStateException("The MQTT Version specified is not supported. Version was " + connectCommand.getVersion());
    }

    private @NotNull Mqtt5AsyncClient connectMqtt5Client(final @NotNull ConnectCommand connectCommand, final String identifier) {
        final MqttClientBuilder clientBuilder = createBuilder(connectCommand, identifier);
        final Mqtt5BlockingClient client = clientBuilder.useMqttVersion5().build().toBlocking();
        final @Nullable Mqtt5Publish willPublish = createMqtt5WillPublish(connectCommand);

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
                .sessionExpiryInterval(connectCommand.getSessionExpiryInterval())
                .keepAlive(connectCommand.getKeepAlive())
                .cleanStart(connectCommand.isCleanStart())
                .willPublish(willPublish);

        applyMqtt5Authentication(connectBuilder, connectCommand);

        mqtt5Connect(client, connectBuilder.build(), connectCommand);

        clientCache.put(connectCommand.getKey(), client.toAsync());
        clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        return client.toAsync();
    }

    private @NotNull Mqtt3AsyncClient connectMqtt3Client(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {
        final MqttClientBuilder clientBuilder = createBuilder(connectCommand, identifier);
        final Mqtt3BlockingClient client = clientBuilder.useMqttVersion3().build().toBlocking();

        final @Nullable Mqtt3Publish willPublish = createMqtt3WillPublish(connectCommand);

        Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder()
                    .keepAlive(connectCommand.getKeepAlive())
                    .cleanSession(connectCommand.isCleanStart())
                    .willPublish(willPublish);

        applyMqtt3Authentication(connectBuilder, connectCommand);

        mqtt3Connect(client, connectBuilder.build(), connectCommand);

        clientCache.put(connectCommand.getKey(), client.toAsync());
        clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        return client.toAsync();
    }

    private @Nullable Mqtt5Publish createMqtt5WillPublish(final @NotNull ConnectCommand connectCommand) {
        // only topic is mandatory for will message creation
        if (connectCommand.getWillTopic() != null) {
            final ByteBuffer willPayload = connectCommand.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willPayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .delayInterval(connectCommand.getWillDelayInterval())
                    .payloadFormatIndicator(connectCommand.getWillPayloadFormatIndicator())
                    .contentType(connectCommand.getWillContentType())
                    .responseTopic(connectCommand.getWillResponseTopic())
                    .correlationData(connectCommand.getWillCorrelationData());
            if (connectCommand.getWillMessageExpiryInterval() != null) {
                builder.messageExpiryInterval(connectCommand.getWillMessageExpiryInterval());
            }
            if (connectCommand.getWillUserProperties() != null) { // user Properties can't be completed with null
                builder.userProperties(connectCommand.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (connectCommand.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());
        }
        return null;
    }

    private @Nullable Mqtt3Publish createMqtt3WillPublish(final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getWillTopic() != null) {
            final ByteBuffer willPayload = connectCommand.getWillMessage();
            return Mqtt3Publish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willPayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .build();
        } else if (connectCommand.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());

        }
        return null;
    }

    private @NotNull MqttClientBuilder createBuilder(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {

        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .sslConfig(connectCommand.getSslConfig())
                .identifier(identifier);
    }

    private void applyMqtt5Authentication(final @NotNull Mqtt5ConnectBuilder connectBuilder, final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getUser() != null && connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getUser() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .applySimpleAuth();
        }
    }

    private void applyMqtt3Authentication(final @NotNull Mqtt3ConnectBuilder connectBuilder, final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getUser() != null && connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getUser() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .applySimpleAuth();
        } else if (connectCommand.getPassword() != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
    }

    public ClientCache<String, MqttClient> getClientCache() {
        return clientCache;
    }

    public Map<String, LocalDateTime> getClientCreationTimes() {
        return clientCreationTimes;
    }

    private MqttClient getMqttClientFromCacheOrConnect(final @NotNull ConnectCommand connect) {
        clientCache.setVerbose(connect.isVerbose());

        MqttClient mqttClient = null;

        if (clientCache.hasKey(connect.getKey())) {
            mqttClient = clientCache.get(connect.getKey());
        }

        if (mqttClient == null || (!mqttClient.getConfig().getState().isConnectedOrReconnect())) {
            mqttClient = connect(connect);
        }
        return mqttClient;
    }
}
