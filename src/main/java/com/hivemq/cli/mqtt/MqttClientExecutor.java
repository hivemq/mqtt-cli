package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.commands.cli.ConnectCommand;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.util.encoders.Base64;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand) {

        if (connectCommand.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("sending CONNECT with Command: {}", connectCommand);
        }

        final Mqtt5ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReasonCode());
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand) {

        if (connectCommand.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("sending CONNECT with Command: {}", connectCommand);
        }

        final Mqtt3ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReturnCode());
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (subscribe.isDebug()) {
            Logger.debug("sending SUBSCRIBE: (Topic: {}, QoS: {})", topic, qos);
        }

        if (subscribe.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Command: {}", subscribe);
        }

        PrintWriter fileWriter = null;
        if (subscribe.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribe.getReceivedMessagesFile());
        }
        final PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) {
                finalFileWriter.close();
            }
        }));


        final Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete builder = client.subscribeWith()
                .topicFilter(topic)
                .qos(qos);

        if (subscribe.getSubscribeUserProperties() != null) {
            builder.userProperties(subscribe.getSubscribeUserProperties());
        }

        builder.callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = applyBase64EncodingIfSet(subscribe.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribe.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribe.isDebug()) {
                        Logger.debug("received PUBLISH: (Topic: {}, Message: '{}')", publish.getTopic(), payloadMessage);
                    }

                    if (subscribe.isVerbose()) {
                        Logger.trace("received PUBLISH: {}", publish);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {

                    if (throwable != null) {
                        if (subscribe.isDebug()) {
                            Logger.debug("SUBSCRIBE failed: {} ", topic, throwable.getStackTrace());
                        }
                        Logger.error("SUBSCRIBE to {} failed with {}", topic, throwable.getMessage());
                    } else {

                        if (subscribe.isDebug()) {
                            Logger.debug("received SUBACK: {}", subAck.getReasonCodes());
                        }

                        if (subscribe.isVerbose()) {
                            Logger.trace("received SUBACK: {}", subAck);
                        }

                    }

                });
    }

    void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (subscribe.isDebug()) {
            Logger.debug("sending SUBSCRIBE: (Topic: {}, QoS: {})", topic, qos);
        }

        if (subscribe.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Command: {}", subscribe);
        }

        PrintWriter fileWriter = null;
        if (subscribe.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribe.getReceivedMessagesFile());
        }
        final PrintWriter finalFileWriter = fileWriter;
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
                    final String payloadMessage = applyBase64EncodingIfSet(subscribe.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribe.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribe.isDebug()) {
                        Logger.debug("received PUBLISH: (Topic: {}, Message: '{}')", publish.getTopic(), payloadMessage);
                    }

                    if (subscribe.isVerbose()) {
                        Logger.trace("received PUBLISH: {}", publish);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                        if (subscribe.isDebug()) {
                            Logger.debug("SUBSCRIBE failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("SUBSCRIBE to {} failed with {}", topic, throwable.getMessage());
                    } else {

                        if (subscribe.isDebug()) {
                            Logger.debug("received SUBACK: {}", subAck.getReturnCodes());
                        }

                        if (subscribe.isVerbose()) {
                            Logger.trace("received SUBACK: {}", subAck);
                        }

                    }
                });
    }

    void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publish.isDebug()) {
            Logger.debug("sending PUBLISH: (Topic: {}, QoS {}, Message: '{}')", topic, qos, bufferToString(publish.getMessage()));
        }

        if (publish.isVerbose()) {
            Logger.trace("sending PUBLISH with command: {}", publish);
        }

        final Mqtt5PublishBuilder.Complete publishBuilder = Mqtt5Publish.builder()
                .topic(topic)
                .qos(qos)
                .retain(publish.isRetain())
                .payload(publish.getMessage())
                .payloadFormatIndicator(publish.getPayloadFormatIndicator())
                .contentType(publish.getContentType())
                .responseTopic(publish.getResponseTopic())
                .correlationData(publish.getCorrelationData());
        if (publish.getMessageExpiryInterval() != null) {
            publishBuilder.messageExpiryInterval(publish.getMessageExpiryInterval());
        }
        if (publish.getPublishUserProperties() != null) {
            publishBuilder.userProperties(publish.getPublishUserProperties());
        }

        final CompletableFuture<Mqtt5PublishResult> publishResultCompletableFuture = client.publish(publishBuilder.build())
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {

                        if (publish.isDebug()) {
                            Logger.debug("PUBLISH failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("PUBLISH to {} failed with {}", topic, throwable.getMessage());

                    } else {

                        final String p = bufferToString(publish.getMessage());

                        if (publish.isDebug()) {
                            Logger.debug("received RESULT: '{}' for PUBLISH to Topic:  {}", trimMessage(p), topic);
                        }

                        if (publish.isVerbose()) {
                            Logger.trace("received RESULT: '{}' for PUBLISH to Topic: {}", publishResult, topic);
                        }

                    }
                });

        if (publish instanceof PublishCommand) {
            publishResultCompletableFuture.join();
        }
    }


    void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publish.isDebug()) {
            Logger.debug("sending PUBLISH: (Topic: {}, QoS {}, Message: '{}')", topic, qos, bufferToString(publish.getMessage()));
        }

        if (publish.isVerbose()) {
            Logger.trace("sending PUBLISH with command: {}", publish);
        }

        final CompletableFuture<Mqtt3Publish> publishCompletableFuture = client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publish.isRetain())
                .payload(publish.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {

                        if (publish.isDebug()) {
                            Logger.debug("PUBLISH failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("PUBLISH to {} failed with {}", topic, throwable.getMessage());

                    } else {

                        final String p = bufferToString(publish.getMessage());

                        if (publish.isDebug()) {
                            Logger.debug("received RESULT: '{}' for PUBLISH to Topic:  {}", trimMessage(p), topic);
                        }

                        if (publish.isVerbose()) {
                            Logger.trace("received RESULT: '{}' for PUBLISH to Topic: {}", publishResult, topic);
                        }

                    }
                });

        if (publish instanceof PublishCommand) {
            publishCompletableFuture.join();
        }

    }

    @Override
    void mqtt5Unsubscribe(@NotNull final Mqtt5Client client, @NotNull final Unsubscribe unsubscribe) {

        if (unsubscribe.isVerbose()) {
            Logger.trace("Sending UNSUBSCRIBE with command: {}", unsubscribe);
        }

        for (String topic : unsubscribe.getTopics()) {

            if (unsubscribe.isDebug()) {
                Logger.debug("sending UNSUBSCRIBE: (Topic: {}, userProperties: {})", topic, unsubscribe.getUserProperties());
            }

            client.toAsync()
                    .unsubscribeWith()
                    .addTopicFilter(topic)
                    .send()
                    .whenComplete((Mqtt5UnsubAck unsubAck, Throwable throwable) -> {

                        if (throwable != null) {
                            if (unsubscribe.isDebug()) {
                                Logger.debug("UNSUBSCRIBE failed: {}", throwable.getStackTrace());
                            }

                            Logger.error("UNSUBSCRIBE to {} failed with ()", topic, throwable.getMessage());
                        } else {

                            if (unsubscribe.isVerbose()) {
                                Logger.trace("received UNSUBACK: {}", unsubAck);
                            }

                            if (unsubscribe.isDebug()) {
                                Logger.debug("received UNSUBACK: {}", unsubAck.getReasonCodes());
                            }

                        }
                    });
        }
    }

    @Override
    void mqtt3Unsubscribe(@NotNull final Mqtt3Client client, @NotNull final Unsubscribe unsubscribe) {

        if (unsubscribe.isVerbose()) {
            Logger.trace("Sending UNSUBSCRIBE with command: {}", unsubscribe);
        }

        for (String topic : unsubscribe.getTopics()) {

            if (unsubscribe.isDebug()) {
                Logger.debug("Sending UNSUBSCRIBE: (Topic: {}, userProperties: {})", topic, unsubscribe.getUserProperties());
            }

            client.toAsync()
                    .unsubscribeWith()
                    .addTopicFilter(topic)
                    .send()
                    .whenComplete((Void unsubAck, Throwable throwable) -> {

                        if (throwable != null) {
                            if (unsubscribe.isDebug()) {
                                Logger.debug("UNSUBSCRIBE failed: {}", throwable.getStackTrace());
                            }

                            Logger.error("UNSUBSCRIBE to {} failed with ()", topic, throwable.getMessage());
                        } else {

                            if (unsubscribe.isVerbose()) {
                                Logger.trace("received UNSUBACK");
                            }

                            if (unsubscribe.isDebug()) {
                                Logger.debug("received UNSUBACK");
                            }

                        }
                    });
        }
    }

    @Override
    void mqtt5Disconnect(@NotNull final Mqtt5Client client, @NotNull final Disconnect disconnect) {

        if (disconnect.isVerbose()) {
            Logger.trace("Sending DISCONNECT with command: {}", disconnect);
        }

        if (disconnect.isDebug()) {
            Logger.debug("Sending DISCONNECT (Reason: {}, sessionExpiryInterval: {}, userProperties: {})", disconnect.getReasonString(), disconnect.getSessionExpiryInterval(), disconnect.getUserProperties());
        }

        final Mqtt5DisconnectBuilder.Send<CompletableFuture<Void>> builder = client.toAsync().disconnectWith()
                .reasonString(disconnect.getReasonString());

        if (disconnect.getSessionExpiryInterval() != null) {
            builder.sessionExpiryInterval(disconnect.getSessionExpiryInterval());
        }

        if (disconnect.getUserProperties() != null) {
            builder.userProperties(disconnect.getUserProperties());
        }

        builder.send();

    }


    @Override
    void mqtt3Disconnect(@NotNull final Mqtt3Client client, @NotNull final Disconnect disconnect) {

        if (disconnect.getSessionExpiryInterval() != null) {
            Logger.warn("Session expiry interval set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
        }
        if (disconnect.getReasonString() != null) {
            Logger.warn("Reason string was set but is unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
        }
        if (disconnect.getUserProperties() != null) {
            Logger.warn("User properties were set but are unused in Mqtt version {}", MqttVersion.MQTT_3_1_1);
        }

        if (disconnect.isVerbose()) {
            Logger.trace("Sending DISCONNECT with command: {}", disconnect);
        }

        if (disconnect.isDebug()) {
            Logger.debug("Sending DISCONNECT");
        }


        client.toAsync().disconnect();

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

    private @NotNull String bufferToString(ByteBuffer b) {
        return new String(b.array(), StandardCharsets.UTF_8);
    }
}
