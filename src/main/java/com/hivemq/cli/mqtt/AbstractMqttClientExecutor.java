package com.hivemq.cli.mqtt;

import com.google.common.eventbus.Subscribe;
import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.DisconnectCommand;
import com.hivemq.cli.commands.PublishCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.client.internal.mqtt.MqttAsyncClient;
import com.hivemq.client.internal.mqtt.MqttBlockingClient;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttWillPublish;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.Log;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMqttClientExecutor {

    private ClientCache<String, MqttClient> clientCache = new ClientCache<>();
    private Map<String, LocalDateTime> clientCreationTimes = new HashMap<>();


    abstract boolean mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract boolean mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos);

    public ClientCache<String, MqttClient> getClientCache() {
        return clientCache;
    }

    public Map<String, LocalDateTime> getClientCreationTimes() {
        return clientCreationTimes;
    }

    public void setClientCreationTimes(Map<String, LocalDateTime> clientCreationTimes) {
        this.clientCreationTimes = clientCreationTimes;
    }

    public MqttClient subscribe(final @NotNull SubscribeCommand subscribeCommand) {

        final MqttClient client = getMqttClientFromCacheOrConnect(subscribeCommand);

        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());


        for (int i = 0; i < subscribeCommand.getTopics().length; i++) {
            final String topic = subscribeCommand.getTopics()[i];
            final MqttQos qos = subscribeCommand.getQos().length > i ? subscribeCommand.getQos()[i] : subscribeCommand.getQos()[0];

            if (client instanceof Mqtt5AsyncClient)
                mqtt5Subscribe((Mqtt5AsyncClient) client, subscribeCommand, topic, qos);
            else if (client instanceof Mqtt3AsyncClient)
                mqtt3Subscribe((Mqtt3AsyncClient) client, subscribeCommand, topic, qos);
            else {
                Logger.error("Can't subscribe with client of type {}", client.getClass());
            }
        }

        return client;
    }

    public MqttClient publish(final @NotNull PublishCommand publishCommand) {
        final MqttClient client = getMqttClientFromCacheOrConnect(publishCommand);
        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < publishCommand.getTopics().length; i++) {
            final String topic = publishCommand.getTopics()[i];
            final MqttQos qos = publishCommand.getQos()[i];

            if (client instanceof Mqtt5AsyncClient)
                mqtt5Publish((Mqtt5AsyncClient) client, publishCommand, topic, qos);
            else if (client instanceof Mqtt3AsyncClient)
                mqtt3Publish((Mqtt3AsyncClient) client, publishCommand, topic, qos);
            else
                Logger.error("Can't publish with client of type {}", client.getClass());
        }
        return client;
    }

    public boolean isConnected(final @NotNull SubscribeCommand subscriber) {
        LoggingContext.put("identifier", subscriber.getIdentifier());

        if (clientCache.hasKey(subscriber.getKey())) {
            final MqttClient client = clientCache.get(subscriber.getKey());
            Logger.debug("Client in cache key: {} ", subscriber.getKey());
            return client.getConfig().getState().isConnected();
        }
        return false;
    }

    public boolean disconnect(final @NotNull DisconnectCommand disconnectCommand) {
        LoggingContext.put("identifier", disconnectCommand.getIdentifier());

        clientCache.setVerbose(disconnectCommand.isDebug());

        if (clientCache.hasKey(disconnectCommand.getKey())) {
            final MqttClient client = clientCache.get(disconnectCommand.getKey());
            clientCache.remove(disconnectCommand.getKey());

            if (client instanceof Mqtt5AsyncClient)
                ((Mqtt5AsyncClient) client).disconnect();
            else if (client instanceof Mqtt3AsyncClient)
                ((Mqtt3AsyncClient) client).disconnect();
            return true;
        }
        return false;
    }


    public MqttClient connect(final @NotNull ConnectCommand connectCommand) {
        final String identifier = connectCommand.createIdentifier();
        LoggingContext.put("identifier", identifier);

        final @NotNull MqttClientBuilder mqttClientBuilder = createBuilder(connectCommand, identifier);

        if (connectCommand.getVersion() == MqttVersion.MQTT_5_0)
            return connectMqtt5Client(mqttClientBuilder, connectCommand);
        else if (connectCommand.getVersion() == MqttVersion.MQTT_3_1_1)
            return connectMqtt3Client(mqttClientBuilder, connectCommand);

        Logger.debug("The MQTT Version specified is not supported - Version was {}", connectCommand.getVersion());

        return null;
    }

    private Mqtt5AsyncClient connectMqtt5Client(final @NotNull MqttClientBuilder clientBuilder, final @NotNull ConnectCommand connectCommand) {
        Mqtt5BlockingClient client = clientBuilder.useMqttVersion5().build().toBlocking();

        try {
            final @Nullable Mqtt5Publish willPublish = createMqtt5WillPublish(connectCommand);

            Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
                    .sessionExpiryInterval(connectCommand.getSessionExpiryInterval())
                    .keepAlive(connectCommand.getKeepAlive())
                    .cleanStart(connectCommand.isCleanStart())
                    .willPublish(willPublish);

            applyAuthentication(connectBuilder, connectCommand);

            final Mqtt5Connect connectMessage = connectBuilder.build();

            mqtt5Connect(client, connectMessage, connectCommand);

            clientCache.put(connectCommand.getKey(), client.toAsync());
            clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        } catch (Exception throwable) {
            Logger.error("Client connect failed with reason: {}", throwable.getMessage());
        }

        return client.toAsync();
    }

    private Mqtt3AsyncClient connectMqtt3Client(final @NotNull MqttClientBuilder clientBuilder, final @NotNull ConnectCommand connectCommand) {

        Mqtt3BlockingClient client = clientBuilder.useMqttVersion3().build().toBlocking();

        try {
            final @Nullable Mqtt3Publish willPublish = createMqtt3WillPublish(connectCommand);

            Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder()
                    .keepAlive(connectCommand.getKeepAlive())
                    .cleanSession(connectCommand.isCleanStart())
                    .willPublish(willPublish);

            final Mqtt3Connect connectMessage = connectBuilder.build();

            mqtt3Connect(client, connectMessage, connectCommand);

            clientCache.put(connectCommand.getKey(), client.toAsync());
            clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        } catch (Exception throwable) {
            Logger.error("Client connect failed with reason: {}", throwable.getMessage());
        }
        return client.toAsync();
    }


    private Mqtt5Publish createMqtt5WillPublish(final @NotNull ConnectCommand connectCommand) {
        // only topic is mandatory for will message creation
        if (connectCommand.getWillTopic() != null) {
            ByteBuffer willpayload = connectCommand.getWillMessage();
            Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willpayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .messageExpiryInterval(connectCommand.getWillMessageExpiryInterval())
                    .delayInterval(connectCommand.getWillDelayInterval())
                    .payloadFormatIndicator(connectCommand.getWillPayloadFormatIndicator())
                    .contentType(connectCommand.getWillContentType())
                    .responseTopic(connectCommand.getWillResponseTopic())
                    .correlationData(connectCommand.getWillCorrelationData());
            if (connectCommand.getWillUserProperties() != null) { // user Properties can't be completed with null
                builder.userProperties(connectCommand.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (connectCommand.getWillMessage() != null) {
            //seems somebody like to create a will message without a topic
            Logger.debug("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());
        }
        return null;
    }

    private Mqtt3Publish createMqtt3WillPublish(final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getWillTopic() != null) {
            ByteBuffer willPayload = connectCommand.getWillMessage();
            return Mqtt3Publish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willPayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .build();
        } else if (connectCommand.getWillMessage() != null) {
            Logger.debug("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());
        }
        return null;
    }

    private MqttClientBuilder createBuilder(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {

        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .sslConfig(connectCommand.getSslConfig())
                .identifier(identifier);
    }

    private MqttClient getMqttClientFromCacheOrConnect(final @NotNull ConnectCommand connect) {
        clientCache.setVerbose(connect.isDebug());

        MqttClient mqttClient = null;

        if (clientCache.hasKey(connect.getKey())) {
            mqttClient = clientCache.get(connect.getKey());
        }

        if (mqttClient == null || (!mqttClient.getConfig().getState().isConnectedOrReconnect())) {
            mqttClient = connect(connect);
        }
        return mqttClient;
    }

    private void applyAuthentication(final @NotNull Mqtt5ConnectBuilder connectBuilder, final @NotNull ConnectCommand connectCommand) {
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
}
