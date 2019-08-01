package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
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
import jline.internal.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;

import java.nio.ByteBuffer;

abstract class AbstractMqttClientExecutor {

    private ClientCache<String, Mqtt5AsyncClient> clientCache = new ClientCache<>();



    abstract boolean mqttConnect(Mqtt5BlockingClient client, Mqtt5Connect connectMessage, Connect connectCommand);

    abstract void mqttSubscribe(Mqtt5AsyncClient client, @NotNull Subscribe subscribe, String topic, MqttQos qos);

    public ClientCache<String, Mqtt5AsyncClient> getClientCache() {
        return clientCache;
    }

    public Mqtt5AsyncClient connect(final @NotNull Connect connect) {
        return doConnect(connect);
    }

    public Mqtt5AsyncClient subscribe(final @NotNull Subscribe subscribe) {
        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(subscribe);
        if (client != null) {
            LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());
            return doSubscribe(client, subscribe);
        }
        return null;
    }

    public Mqtt5AsyncClient publish(final @NotNull Publish publish) {
        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(publish);
        if (client != null) {
            LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());
            return doPublish(client, publish);
        }
        return null;
    }

    public boolean isConnected(final @NotNull Subscribe subscriber) {
        LoggingContext.put("identifier", subscriber.getIdentifier());

        if (clientCache.hasKey(subscriber.getKey())) {
            final Mqtt5AsyncClient client = clientCache.get(subscriber.getKey());
            Logger.debug("Client in cache key: {} ", subscriber.getKey());
            return client.getConfig().getState().isConnected();
        }
        return false;
    }

    public boolean disconnect(final @NotNull Disconnect disconnect) {
        LoggingContext.put("identifier", disconnect.getIdentifier());

        clientCache.setVerbose(disconnect.isDebug());

        if (clientCache.hasKey(disconnect.getKey())) {
            final Mqtt5AsyncClient client = clientCache.get(disconnect.getKey());
            clientCache.remove(disconnect.getKey());
            client.disconnect();
            return true;
        }
        return false;
    }


    private Mqtt5AsyncClient doConnect(final @NotNull Connect connectCommand) {
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

        } catch (Exception throwable) {
            Logger.error("Client connect failed with reason: {}", throwable.getMessage());
        }


        return client.toAsync();
    }


    private Mqtt5AsyncClient doSubscribe(Mqtt5AsyncClient client, final @NotNull Subscribe subscribe) {

        for (int i = 0; i < subscribe.getTopics().length; i++) {
            final String topic = subscribe.getTopics()[i];
            final MqttQos qos = subscribe.getQos()[i];

            mqttSubscribe(client, subscribe, topic, qos);

        }

        return client;
    }


    private Mqtt5AsyncClient doPublish(Mqtt5AsyncClient client, final @NotNull Publish publish) {
        for (int i = 0; i < publish.getTopics().length; i++) {
            final String topic = publish.getTopics()[i];
            final MqttQos qos = publish.getQos()[i];

            client.publishWith()
                    .topic(topic)
                    .qos(qos)
                    .retain(publish.isRetain())
                    .payload(publish.getMessage())
                    .send()
                    .whenComplete((publishResult, throwable) -> {
                        if (throwable != null) {
                            if (publish.isDebug()) {
                                Log.debug("Client publish to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                            } else {
                                Logger.error("Client publish to topic: {} failed with reason: {}", topic, throwable.getMessage());
                            }
                        } else {
                            final String p = publish.getMessage().toString();
                            if (publish.isDebug()) {
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

    private Mqtt5Publish createWillPublish(final @NotNull Connect connectCommand) throws Exception {
        // only topic is mandatory for will message creation
        if (connectCommand.getWillTopic() != null) {
            ByteBuffer willpayload = connectCommand.getWillMessage() != null ? connectCommand.getWillMessage() : null;
            Mqtt5WillPublishBuilder builder = Mqtt5WillPublish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willpayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .messageExpiryInterval(connectCommand.getWillMessageExpiryInterval())
                    .delayInterval(connectCommand.getWillDelayInterval())
                    .payloadFormatIndicator(connectCommand.getWillPayloadFormatIndicator())
                    .contentType(connectCommand.getWillContentType())
                    .responseTopic(connectCommand.getWillResponseTopic())
                    .correlationData(connectCommand.getWillCorrelationData())
                    .userProperties(connectCommand.getWillUserProperties());
            try {
                return ((Mqtt5WillPublishBuilder.Complete) builder).build().asWill();
            } catch (Exception e) {
                Logger.error("Client can't create Will Message, error: {} " + e.getMessage());
                throw e;
            }
        } else if (connectCommand.getWillMessage() != null) {
            //seems somebody like to create a will message without a topic
            Logger.debug("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());
        }
        return null;
    }

    private MqttClientBuilder createBuilder(final @NotNull Connect connectCommand, final @NotNull String identifier) {
        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .identifier(identifier);
    }

    private Mqtt5AsyncClient getMqttClientFromCacheOrConnect(final @NotNull Connect connect) {
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

    private void applyAuthentication(final @NotNull Mqtt5ConnectBuilder connectBuilder, final @NotNull Connect connectCommand) {
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
