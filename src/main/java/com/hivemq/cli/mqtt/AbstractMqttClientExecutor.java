package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.DisconnectCommand;
import com.hivemq.cli.commands.PublishCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
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

    private ClientCache<String, Mqtt5AsyncClient> clientCache = new ClientCache<>();
    private Map<String, LocalDateTime> clientCreationTimes = new HashMap<>();


    abstract boolean mqttConnect(Mqtt5BlockingClient client, Mqtt5Connect connectMessage, ConnectCommand connectCommand);

    abstract void mqttSubscribe(Mqtt5AsyncClient client, @NotNull SubscribeCommand subscribeCommand, String topic, MqttQos qos);

    public ClientCache<String, Mqtt5AsyncClient> getClientCache() {
        return clientCache;
    }

    public Map<String, LocalDateTime> getClientCreationTimes() {
        return clientCreationTimes;
    }

    public void setClientCreationTimes(Map<String, LocalDateTime> clientCreationTimes) {
        this.clientCreationTimes = clientCreationTimes;
    }

    public Mqtt5AsyncClient connect(final @NotNull ConnectCommand connect) {
        return doConnect(connect);
    }

    public Mqtt5AsyncClient subscribe(final @NotNull SubscribeCommand subscribeCommand) {

        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(subscribeCommand);
        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());
        return doSubscribe(client, subscribeCommand);

    }

    public Mqtt5AsyncClient publish(final @NotNull PublishCommand publishCommand) {

        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(publishCommand);
        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());
        return doPublish(client, publishCommand);

    }

    public boolean isConnected(final @NotNull SubscribeCommand subscriber) {
        LoggingContext.put("identifier", subscriber.getIdentifier());

        if (clientCache.hasKey(subscriber.getKey())) {
            final Mqtt5AsyncClient client = clientCache.get(subscriber.getKey());
            Logger.debug("Client in cache key: {} ", subscriber.getKey());
            return client.getConfig().getState().isConnected();
        }
        return false;
    }

    public boolean disconnect(final @NotNull DisconnectCommand disconnectCommand) {
        LoggingContext.put("identifier", disconnectCommand.getIdentifier());

        clientCache.setVerbose(disconnectCommand.isDebug());

        if (clientCache.hasKey(disconnectCommand.getKey())) {
            final Mqtt5AsyncClient client = clientCache.get(disconnectCommand.getKey());
            clientCache.remove(disconnectCommand.getKey());
            client.disconnect();
            return true;
        }
        return false;
    }


    private Mqtt5AsyncClient doConnect(final @NotNull ConnectCommand connectCommand) {
        final String identifier = connectCommand.createIdentifier();
        LoggingContext.put("identifier", identifier);

        final @NotNull MqttClientBuilder mqttClientBuilder = createBuilder(connectCommand, identifier);
        final @NotNull Mqtt5BlockingClient client = mqttClientBuilder.useMqttVersion5().build().toBlocking();


        try {
            final @Nullable Mqtt5Publish willPublish = createWillPublish(connectCommand);

            Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
                    .sessionExpiryInterval(connectCommand.getSessionExpiryInterval())
                    .keepAlive(connectCommand.getKeepAlive())
                    .cleanStart(connectCommand.isCleanStart())
                    .willPublish(willPublish);

            applyAuthentication(connectBuilder, connectCommand);

            final Mqtt5Connect connectMessage = connectBuilder.build();

            mqttConnect(client, connectMessage, connectCommand);

            clientCache.put(connectCommand.getKey(), client.toAsync());
            clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        } catch (Exception throwable) {
            Logger.error("Client connect failed with reason: {}", throwable.getMessage());
        }


        return client.toAsync();
    }


    private Mqtt5AsyncClient doSubscribe(Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand) {

        for (int i = 0; i < subscribeCommand.getTopics().length; i++) {
            final String topic = subscribeCommand.getTopics()[i];
            final MqttQos qos = subscribeCommand.getQos().length > i ? subscribeCommand.getQos()[i] : subscribeCommand.getQos()[0];

            mqttSubscribe(client, subscribeCommand, topic, qos);

        }

        return client;
    }


    private Mqtt5AsyncClient doPublish(Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand) {
        for (int i = 0; i < publishCommand.getTopics().length; i++) {
            final String topic = publishCommand.getTopics()[i];
            final MqttQos qos = publishCommand.getQos()[i];

            client.publishWith()
                    .topic(topic)
                    .qos(qos)
                    .retain(publishCommand.isRetain())
                    .payload(publishCommand.getMessage())
                    .send()
                    .whenComplete((publishResult, throwable) -> {
                        if (throwable != null) {
                            if (publishCommand.isDebug()) {
                                Log.debug("Client publish to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                            } else {
                                Logger.error("Client publish to topic: {} failed with reason: {}", topic, throwable.getMessage());
                            }
                        } else {
                            final String p = publishCommand.getMessage().toString();
                            if (publishCommand.isDebug()) {
                                Log.debug("Client publish to topic: {} message: '{}' ", topic, p);
                            } else {
                                Logger.info("Client publish to topic: {} message: '{}... ' ", topic,
                                        p.length() > 10 ? p.substring(0, 10) : p);
                            }
                        }
                    });
        }
        return client;
    }

    private Mqtt5Publish createWillPublish(final @NotNull ConnectCommand connectCommand) throws Exception {
        // only topic is mandatory for will message creation
        if (connectCommand.getWillTopic() != null) {
            ByteBuffer willpayload = connectCommand.getWillMessage() != null ? connectCommand.getWillMessage() : null;
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

    private MqttClientBuilder createBuilder(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {

        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .sslConfig(connectCommand.getSslConfig())
                .identifier(identifier);
    }

    private Mqtt5AsyncClient getMqttClientFromCacheOrConnect(final @NotNull ConnectCommand connect) {
        clientCache.setVerbose(connect.isDebug());

        Mqtt5AsyncClient mqtt5Client = null;

        if (clientCache.hasKey(connect.getKey())) {
            mqtt5Client = clientCache.get(connect.getKey());
        }

        if (mqtt5Client == null || (!mqtt5Client.getConfig().getState().isConnectedOrReconnect())) {
            mqtt5Client = doConnect(connect);
        }
        return mqtt5Client;
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
