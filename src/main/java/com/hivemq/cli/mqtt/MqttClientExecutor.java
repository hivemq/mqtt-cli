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

        if (connectCommand.isDebug()) {
            Logger.debug("Client connects with {} ", connectCommand);
        } else {
            Logger.info("Client connects with {} ", connectMessage);
        }

        Mqtt5ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("Client received {} ", connAck);
        } else {
            Logger.info("Client received {} ", connAck.getReasonCode());
        }
        client.getConfig().getState();
    }

    void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand) {

        if (connectCommand.isDebug()) {
            Logger.debug("Client connects with {} ", connectCommand);
        } else {
            Logger.info("Client connects with {} ", connectMessage);
        }

        Mqtt3ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("Client received {} ", connAck);
        } else {
            Logger.info("Client received {} ", connAck.getReturnCode());
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

        if (subscribeCommand.isDebug()) {
            Logger.debug("Client subscribe with {}", subscribeCommand);
        } else {
            Logger.info("Client subscribe to Topic: {}", topic);
        }

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
                        Logger.debug("Client received on topic: {} message: '{}' ", publish.getTopic(), payloadMessage);
                    } else {
                        Logger.info("Client received on topic: {} message: '{}'' ", publish.getTopic(), trimMessage(payloadMessage));
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Logger.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else if (subscribeCommand.isDebug()) {
                        Logger.debug("Client received {}", subAck);
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

        if (subscribeCommand.isDebug()) {
            Logger.debug("Client subscribe with {}", subscribeCommand);
        } else {
            Logger.info("Client subscribe to Topic: {}", topic);
        }


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
                        Logger.debug("Client received on topic: {} message: '{}' ", publish.getTopic(), payloadMessage);
                    } else {
                        Logger.info("Client received on topic: {} message: '{}'' ", publish.getTopic(), trimMessage(payloadMessage));
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Logger.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else if (subscribeCommand.isDebug()) {
                        Logger.debug("Client received {}", subAck);
                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }

    void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publishCommand.isDebug()) {
            Logger.debug("Client publish with: {}", publishCommand);
        } else {
            Logger.info("Client publish with: topic: {}, qos: {}, message: '{}'", topic, qos, publishCommand.getMessage().toString());
        }

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        if (publishCommand.isDebug()) {
                            Logger.debug("Client publish to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client publish to topic: {} failed with reason: {}", topic, throwable.getMessage());
                        }
                    } else {
                        final String p = publishCommand.getMessage().toString();
                        if (publishCommand.isDebug()) {
                            Logger.debug("Client published to topic: {} received {} ", topic, publishResult);
                        } else {
                            Logger.info("Client published to topic: {} message: '{}... ' ", topic, trimMessage(p));
                        }
                    }
                });
    }


    void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publishCommand.isDebug()) {
            Logger.debug("Client publish with: {}", publishCommand);
        } else {
            Logger.info("Client publish with: topic: {}, qos: {}, message: '{}'", topic, qos, publishCommand.getMessage().toString());
        }

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        if (publishCommand.isDebug()) {
                            Logger.debug("Client publish to topic: {} failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client publish to topic: {} failed with reason: {}", topic, throwable.getMessage());
                        }
                    } else {
                        final String p = publishCommand.getMessage().toString();
                        if (publishCommand.isDebug()) {
                            Logger.debug("Client published to topic: {} received {} ", topic, publishResult);
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
