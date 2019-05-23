package com.hivemq.cli.util;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MqttUtils {

    private static final MqttUtils instance = new MqttUtils();
    private ClientCache<String, Mqtt5AsyncClient> mqttClientClientCache;

    private MqttUtils() {
        mqttClientClientCache = new ClientCache<>(false);
    }

    public static MqttUtils getInstance() {
        return instance;
    }

    private static MqttQos getQosFromParamField(int[] qos, int i) {
        if (qos == null || qos.length <= i) {
            return MqttQos.AT_MOST_ONCE;
        }
        return getQosFromInt(qos[i]);
    }

    private static MqttQos getQosFromInt(int qos) {
        if (qos == 0) {
            return MqttQos.AT_MOST_ONCE;
        }
        return qos == 1 ? MqttQos.AT_LEAST_ONCE : MqttQos.EXACTLY_ONCE;
    }


    public Mqtt5AsyncClient connect(Connect connect) throws Exception {
        return doConnect(connect);
    }

    public Mqtt5AsyncClient subscribe(Subscribe subscribe) throws Exception {
        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(subscribe);
        if (client != null) {
            return doSubscribe(client, subscribe);
        }
        return null;
    }
    public Mqtt5AsyncClient publish(Publish publish) throws Exception {
        final Mqtt5AsyncClient client = getMqttClientFromCacheOrConnect(publish);
        if (client != null) {
            return doPublish(client, publish);
        }
        return null;
    }

    private Mqtt5AsyncClient doConnect(final @NotNull Connect connectCommand) {
        final String identifier = connectCommand.createIdentifier();
        final MqttClientBuilder mqttClientBuilder = createBuilder(connectCommand, identifier);
        final Mqtt5AsyncClient client = mqttClientBuilder.useMqttVersion5().build().toAsync();


        final @Nullable Mqtt5Publish willPublish = createWillPublish(connectCommand, identifier);
        client.connectWith().willPublish(willPublish).send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Connect Client:" + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
                    } else {
                        mqttClientClientCache.put(connectCommand.getKey(), client);
                        System.out.println("Connect Client:" + identifier + " with " + connAck.getReasonCode());
                    }
                });

        return client;
    }

    private Mqtt5AsyncClient doSubscribe(Mqtt5AsyncClient client, Subscribe subscribe) {

        for (int i = 0; i < subscribe.getTopics().length; i++) {
            final String topic = subscribe.getTopics()[i];
            final MqttQos qos = getQosFromParamField(subscribe.getQos(), i);

            client.subscribeWith()
                    .topicFilter(topic)
                    .qos(qos)
                    .callback(publish -> {
                        System.out.println("Client::" + client.getConfig().getClientIdentifier().get() + " received msg:" + new String(publish.getPayloadAsBytes()));
                    })
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            System.err.println("Client::" + client.getConfig().getClientIdentifier().get() + " subscribe error: " + throwable.getMessage());
                        } else {
                            System.out.println("Client::" + client.getConfig().getClientIdentifier().get() + " subscribed to Topic: " + topic);
                        }
                    });


        }

        return client;
    }

    private Mqtt5AsyncClient doPublish(Mqtt5AsyncClient client, Publish publish) {
        for (int i = 0; i < publish.getTopics().length; i++) {
            final String topic = publish.getTopics()[i];
            final MqttQos qos = getQosFromParamField(publish.getQos(), i);

            client.publishWith()
                    .topic(topic)
                    .qos(qos)
                    .payload(publish.getMessage().getBytes())
                    .send()
                    .whenComplete((publishResult, throwable) -> {
                        if (throwable != null) {
                            System.err.println("ERROR - publish to topic: " + topic + " failed: " + throwable.getMessage());
                        } else {
                            System.out.println("Client::" +
                                    client.getConfig().getClientIdentifier().get() + " published msg '" + publish.getMessage().substring(0, 10) + "... ' to topic: " + topic);
                        }
                    });
        }
        return client;
    }

    public boolean disconnect(Disconnect disconnect) {
        mqttClientClientCache.setVerbose(disconnect.isDebug());

        if (mqttClientClientCache.hasKey(disconnect.getKey())) {
            final Mqtt5AsyncClient client = mqttClientClientCache.get(disconnect.getKey());
            mqttClientClientCache.remove(disconnect.getKey());
            client.disconnect();
            return true;
        }
        return false;
    }



    private Mqtt5Publish createWillPublish(final @NotNull Connect connectCommand, final @NotNull String identifier) {
        if (connectCommand.getWillTopic() != null) {
            Mqtt5WillPublishBuilder builder = Mqtt5WillPublish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(connectCommand.getWillMessage().getBytes())
                    .qos(getQosFromInt(connectCommand.getWillQos()))
                    .retain(connectCommand.isWillRetain());
            try {
                return ((Mqtt5WillPublishBuilder.Complete) builder).build().asWill();
            } catch (Exception e) {
                System.err.println(" Client::" + identifier + ": can't create Will Message, error:" + e.getMessage());
            }
        }
        return null;
    }


    private MqttClientBuilder createBuilder(Connect connectCommand, final @NotNull String identifier) {
        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .identifier(identifier);
    }

    private Mqtt5AsyncClient getMqttClientFromCacheOrConnect(Connect connect) throws Exception {
        mqttClientClientCache.setVerbose(connect.isDebug());

        Mqtt5AsyncClient mqtt5Client = null;

        if (mqttClientClientCache.hasKey(connect.getKey())) {
            mqtt5Client = mqttClientClientCache.get(connect.getKey());
        }

        if (mqtt5Client == null || (!mqtt5Client.getConfig().getState().isConnectedOrReconnect())) {
            mqtt5Client = doConnect(connect);
        }
        return mqtt5Client;
    }

}
