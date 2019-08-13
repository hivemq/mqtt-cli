package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.PrintWriter;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    private static MqttClientExecutor instance = null;

    @Inject
    MqttClientExecutor() {
    }

    boolean mqttConnect(final @NotNull Mqtt5BlockingClient client, Mqtt5Connect connectMessage, final @NotNull Connect connectCommand) {
        Mqtt5ConnAck connAck = client.connect(connectMessage);
        if (connectCommand.isDebug()) {
            Log.debug("Client connect with {} ", connectCommand.toString());
        } else {
            Logger.info("Client connect with {} ", connAck.getReasonCode());
        }
        return client.getConfig().getState().isConnected();
    }

    void mqttSubscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final String topic, final MqttQos qos) {

        PrintWriter writer = null;
        if (subscribe.getReceivedMessagesFile() != null) {
            writer = FileUtils.createFileAppender(subscribe.getReceivedMessagesFile());
        }
        PrintWriter finalWriter = writer;

        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    final String p = new String(publish.getPayloadAsBytes());

                    if (finalWriter != null) {
                        finalWriter.println(topic + "/: " + p);
                        finalWriter.flush();
                    }

                    if (subscribe.isPrintToSTDOUT()) {
                        System.out.println(p);
                    }

                    if (subscribe.isDebug()) {
                        Log.debug("Client received on topic: {} message: '{}' ", topic, p);
                    } else {
                        Logger.info("Client received msg: '{}...' ", p.length() > 10 ? p.substring(0, 10) : p);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribe.isDebug()) {
                            Log.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }

    private void connectWithSSL(final @NotNull Connect setting, KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) {

        MqttClientSslConfig clientSslConfig = MqttClientSslConfig.builder()
                .trustManagerFactory(trustManagerFactory)   // the truststore
                .keyManagerFactory(keyManagerFactory)       // if a client keyStore is used
                .build();

        final Mqtt5AsyncClient client = MqttClient.builder()
                .identifier("hive-mqtt5-test-client")
                .serverPort(setting.getPort())
                .serverHost(setting.getHost())
                .useSsl(clientSslConfig)
                .useMqttVersion5()
                .buildAsync();

        client.connectWith()
                .keepAlive(setting.getKeepAlive())
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable.getStackTrace());
                    } else {
                        Logger.info("Client {} connected {} ", client.getConfig().getClientIdentifier(), connAck.getReasonCode());
                    }
                });

    }
}
