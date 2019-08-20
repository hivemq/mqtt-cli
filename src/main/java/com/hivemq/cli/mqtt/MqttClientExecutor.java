package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.PublishCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;

import org.bouncycastle.util.encoders.Base64;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand) {
        Mqtt5ConnAck connAck = client.connect(connectMessage);
        if (connectCommand.isDebug()) {
            Log.debug("Client connect with {} ", connectCommand.toString());
        } else {
            Logger.info("Client connect with {} ", connAck.getReasonCode());
        }
        client.getConfig().getState();
    }

    void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand) {
        Mqtt3ConnAck connAck = client.connect(connectMessage);
        if (connectCommand.isDebug()) {
            Log.debug("Client connect with {} ", connectCommand.toString());
        } else {
            Logger.info("Client connect with {} ", connAck.getReturnCode());
        }
        client.getConfig().getState();
    }

    void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        PrintWriter fileWriter = null;
        if (subscribeCommand.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribeCommand.getReceivedMessagesFile());
        }
        PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) {
                finalFileWriter.close();
            }
        }));


        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = applyBase64EncodingIfSet(subscribeCommand.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribeCommand.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribeCommand.isDebug()) {
                        Log.debug("Client received on topic: {} message: '{}' ", topic, payloadMessage);
                    } else {
                        Logger.info("Client received msg: '{}...' ", payloadMessage.length() > 10 ? trimMessage(payloadMessage) : payloadMessage);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Log.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }

    void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        PrintWriter fileWriter = null;
        if (subscribeCommand.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribeCommand.getReceivedMessagesFile());
        }
        PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) {
                finalFileWriter.close();
            }
        }));


        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = applyBase64EncodingIfSet(subscribeCommand.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribeCommand.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribeCommand.isDebug()) {
                        Log.debug("Client received on topic: {} message: '{}' ", topic, payloadMessage);
                    } else {
                        Logger.info("Client received msg: '{}...' ", payloadMessage.length() > 10 ? trimMessage(payloadMessage) : payloadMessage);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Log.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }

    void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        if (publishCommand.isDebug()) {
                            Log.debug("Client published to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client published to topic: {} failed with reason: {}", topic, throwable.getMessage());
                        }
                    } else {
                        final String p = publishCommand.getMessage().toString();
                        if (publishCommand.isDebug()) {
                            Log.debug("Client published to topic: {} message: '{}' ", topic, p);
                        } else {
                            Logger.info("Client published to topic: {} message: '{}... ' ", topic, trimMessage(p));
                        }
                    }
                });
    }


    void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        if (publishCommand.isDebug()) {
                            Log.debug("Client published to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client published to topic: {} failed with reason: {}", topic, throwable.getMessage());
                        }
                    } else {
                        final String p = publishCommand.getMessage().toString();
                        if (publishCommand.isDebug()) {
                            Log.debug("Client published to topic: {} message: '{}' ", topic, p);
                        } else {
                            Logger.info("Client published to topic: {} message: '{}... ' ", topic, trimMessage(p));
                        }
                    }
                });
    }


    private @NotNull String applyBase64EncodingIfSet(final boolean encode, final byte[] payload) {
        if (encode) {
            return Base64.toBase64String(payload);
        } else {
            return new String(payload);
        }
    }

    private @NotNull String trimMessage(String p) {
        if (p.length() > 10) {
            return p.substring(0, 10);
        } else {
            return p;
        }

    }
}
