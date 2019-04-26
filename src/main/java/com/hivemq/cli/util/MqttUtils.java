package com.hivemq.cli.util;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;

import java.util.List;

public class MqttUtils {

    private static final MqttUtils instance = new MqttUtils();
    private ClientCache<String, Mqtt5BlockingClient> mqttClientClientCache;

    private MqttUtils() {
        mqttClientClientCache = new ClientCache<>(false);
    }

    public static MqttUtils getInstance() {
        return instance;
    }

    private static MqttQos getQosFromParam(int[] qos, int i) {
        if (qos.length <= i || qos[i] == 0) {
            return MqttQos.AT_MOST_ONCE;
        }
        return qos[i] == 1 ? MqttQos.AT_LEAST_ONCE : MqttQos.EXACTLY_ONCE;
    }

    public Mqtt5BlockingClient connect(Connect connect) throws Exception {
        return doConnect(connect);
    }

    public Mqtt5BlockingClient subscribe(Subscribe subscribe) throws Exception {
        final Mqtt5BlockingClient mqttBlockingClient = getMqttClientFromCacheOrConnect(subscribe);
        try {
            for (int i = 0; i < subscribe.getTopics().length; i++) {
                final String topic = subscribe.getTopics()[i];
                final MqttQos qos = getQosFromParam(subscribe.getQos(), i);

                List<Mqtt5SubAckReasonCode> returnCodes =
                        (mqttBlockingClient).subscribeWith()
                                .topicFilter(topic)
                                .qos(qos)
                                .send().getReasonCodes();

                System.out.println("Client::" + mqttBlockingClient.getConfig().getClientIdentifier().get() + " subscribed to Topic: " + topic + " with result: " + returnCodes);
            }

        } catch (Mqtt5SubAckException ex) {
            System.err.println((ex.getMqttMessage()).getReasonCodes() +
                    "  " + (ex.getMqttMessage().getReasonString().isPresent() ?
                    ex.getMqttMessage().getReasonString().get() : "")
            );
        }
        return mqttBlockingClient;
    }

    public void publish(Publish publish) throws Exception {
        final Mqtt5BlockingClient mqttBlockingClient = getMqttClientFromCacheOrConnect(publish);
        try {
            for (int i = 0; i < publish.getTopics().length; i++) {
                final String topic = publish.getTopics()[i];
                final MqttQos qos = getQosFromParam(publish.getQos(), i);

                Mqtt5PublishResult returnCodes =
                        (mqttBlockingClient).publishWith()
                                .topic(topic)
                                .qos(qos)
                                .payload(publish.getMessage().getBytes())
                                .send();

                System.out.println("Client::" +
                        mqttBlockingClient.getConfig().getClientIdentifier().get() + " published to Topic: " +
                        topic + " with result: " + returnCodes);
            }

        } catch (Mqtt5SubAckException ex) {
            System.err.println((ex.getMqttMessage()).getReasonCodes() +
                    "  " + (ex.getMqttMessage().getReasonString().isPresent() ?
                    ex.getMqttMessage().getReasonString().get() : "")
            );
        }
    }

    public boolean disconnect(Disconnect disconnect) throws Exception {
        mqttClientClientCache.setVerbose(disconnect.isDebug());

        if (mqttClientClientCache.hasKey(disconnect.getKey())) {
            final Mqtt5BlockingClient mqttBlockingClient = mqttClientClientCache.get(disconnect.getKey());
            mqttClientClientCache.remove(disconnect.getKey());
            mqttBlockingClient.disconnect();
            return true;
        }
        return false;

    }

    private Mqtt5BlockingClient doConnect(Connect connect) {
        final MqttClientBuilder mqttClientBuilder = createBuilder(connect);
        final Mqtt5BlockingClient mqttBlockingClient = mqttClientBuilder.useMqttVersion5().build().toBlocking();

        try {
            System.out.print("Connect::");
            mqttBlockingClient.connect();

            System.out.println("Client::" + (mqttBlockingClient.getConfig().getClientIdentifier().isPresent() ?
                    mqttBlockingClient.getConfig().getClientIdentifier().get() : ""));

            mqttClientClientCache.put(connect.getKey(), mqttBlockingClient);

        } catch (Mqtt5ConnAckException ex) {
            System.err.println((ex.getMqttMessage()).getReasonCode() +
                    "  " + (ex.getMqttMessage().getReasonString().isPresent() ?
                    ex.getMqttMessage().getReasonString().get() : "")
            );

        }
        return mqttBlockingClient;
    }

    private MqttClientBuilder createBuilder(Connect connect) {
        return MqttClient.builder()
                .serverHost(connect.getHost())
                .serverPort(connect.getPort())
                .identifier(connect.createIdentifier());
    }

    private Mqtt5BlockingClient getMqttClientFromCacheOrConnect(Connect connect) throws Exception {
        mqttClientClientCache.setVerbose(connect.isDebug());

        Mqtt5BlockingClient mqttBlockingClient = null;

        if (mqttClientClientCache.hasKey(connect.getKey())) {
            mqttBlockingClient = mqttClientClientCache.get(connect.getKey());
        }

        if (mqttBlockingClient == null || (!mqttBlockingClient.getConfig().getState().isConnectedOrReconnect())) {
            mqttBlockingClient = doConnect(connect);
        }
        return mqttBlockingClient;
    }

}
