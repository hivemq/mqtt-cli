/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.disconnect.Mqtt3Disconnect;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
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
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
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

    void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull Connect connect) {

        if (connect.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connect.isVerbose()) {
            Logger.trace("sending CONNECT with Mqtt5Connect: {}", connectMessage);
        }

        final Mqtt5ConnAck connAck = client.connect(connectMessage);

        if (connect.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReasonCode());
        }

        if (connect.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull Connect connect) {

        if (connect.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connect.isVerbose()) {
            Logger.trace("sending CONNECT with Mqtt3Connect: {}", connectMessage);
        }

        final Mqtt3ConnAck connAck = client.connect(connectMessage);

        if (connect.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReturnCode());
        }

        if (connect.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (subscribe.isDebug()) {
            Logger.debug("sending SUBSCRIBE: (Topic: {}, QoS: {})", topic, qos);
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

        final Mqtt5SubscribeBuilder.Start.Complete builder = Mqtt5Subscribe.builder()
                .topicFilter(topic)
                .qos(qos);

        if (subscribe.getUserProperties() != null) {
            builder.userProperties(subscribe.getUserProperties());
        }


        final Mqtt5Subscribe subscribeMessage = builder.build();

        if (subscribe.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Mqtt5Subscribe: {}", subscribeMessage);
        }

        client.subscribe(subscribeMessage, publish -> {

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

        final Mqtt3SubscribeBuilder.Start.Complete builder = Mqtt3Subscribe.builder()
                .topicFilter(topic)
                .qos(qos);


        final Mqtt3Subscribe subscribeMessage = builder.build();

        if (subscribe.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Mqtt3Subscribe: {}", subscribeMessage);
        }

        client.subscribe(subscribeMessage, publish -> {

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

        final Mqtt5PublishBuilder.Complete publishBuilder = Mqtt5Publish.builder()
                .topic(topic)
                .qos(qos)
                .payload(publish.getMessage())
                .payloadFormatIndicator(publish.getPayloadFormatIndicator())
                .contentType(publish.getContentType())
                .responseTopic(publish.getResponseTopic())
                .correlationData(publish.getCorrelationData());

        if (publish.getRetain() != null) {
            publishBuilder.retain(publish.getRetain());
        }
        if (publish.getMessageExpiryInterval() != null) {
            publishBuilder.messageExpiryInterval(publish.getMessageExpiryInterval());
        }
        if (publish.getUserProperties() != null) {
            publishBuilder.userProperties(publish.getUserProperties());
        }

        final Mqtt5Publish publishMessage = publishBuilder.build();

        if (publish.isVerbose()) {
            Logger.trace("sending PUBLISH with Mqtt5Publish: {}", publishMessage);
        }

        final CompletableFuture<Mqtt5PublishResult> publishResultCompletableFuture = client.publish(publishMessage)
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

        final Mqtt3PublishBuilder.Complete publishBuilder = Mqtt3Publish.builder()
                .topic(topic)
                .qos(qos)
                .payload(publish.getMessage());

        if (publish.getRetain() != null) {
            publishBuilder.retain(publish.getRetain());
        }

        final Mqtt3Publish publishMessage = publishBuilder.build();

        if (publish.isVerbose()) {
            Logger.trace("sending PUBLISH with Mqtt3Publish: {}", publishMessage);
        }

        final CompletableFuture<Mqtt3Publish> publishCompletableFuture = client.publish(publishMessage)
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



        for (String topic : unsubscribe.getTopics()) {

            if (unsubscribe.isDebug()) {
                Logger.debug("sending UNSUBSCRIBE: (Topic: {}, userProperties: {})", topic, unsubscribe.getUserProperties());
            }

            final Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder()
                    .topicFilter(topic)
                    .build();

            if (unsubscribe.isVerbose()) {
                Logger.trace("Sending UNSUBSCRIBE with Mqtt5Unsubscribe: {}", unsubscribeMessage);
            }

            client.toAsync()
                    .unsubscribe(unsubscribeMessage)
                    .whenComplete((Mqtt5UnsubAck unsubAck, Throwable throwable) -> {

                        if (throwable != null) {
                            if (unsubscribe.isDebug()) {
                                Logger.debug("UNSUBSCRIBE failed: {}", throwable);
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


        for (String topic : unsubscribe.getTopics()) {

            if (unsubscribe.isDebug()) {
                Logger.debug("Sending UNSUBSCRIBE: (Topic: {}, userProperties: {})", topic, unsubscribe.getUserProperties());
            }

            final Mqtt3Unsubscribe unsubscribeMessage = Mqtt3Unsubscribe.builder()
                    .topicFilter(topic)
                    .build();

            if (unsubscribe.isVerbose()) {
                Logger.trace("Sending UNSUBSCRIBE with Mqtt3Unsubscribe: {}", unsubscribeMessage);
            }

            client.toAsync()
                    .unsubscribe(unsubscribeMessage)
                    .whenComplete((Void unsubAck, Throwable throwable) -> {

                        if (throwable != null) {
                            if (unsubscribe.isDebug()) {
                                Logger.debug("UNSUBSCRIBE failed: {}", throwable);
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

        if (disconnect.isDebug()) {
            Logger.debug("Sending DISCONNECT (Reason: {}, sessionExpiryInterval: {}, userProperties: {})", disconnect.getReasonString(), disconnect.getSessionExpiryInterval(), disconnect.getUserProperties());
        }


        final Mqtt5DisconnectBuilder disconnectBuilder = Mqtt5Disconnect.builder();

        if (disconnect.getReasonString() != null) {
            disconnectBuilder.reasonString(disconnect.getReasonString());
        }

        if (disconnect.getSessionExpiryInterval() != null) {
            disconnectBuilder.sessionExpiryInterval(disconnect.getSessionExpiryInterval());
        }

        if (disconnect.getUserProperties() != null) {
            disconnectBuilder.userProperties(disconnect.getUserProperties());
        }

        final Mqtt5Disconnect disconnectMessage = disconnectBuilder.build();

        if (disconnect.isVerbose()) {
            Logger.trace("Sending DISCONNECT with Mqtt5Disconnect: {}", disconnectMessage);
        }

        client.toAsync()
                .disconnect(disconnectMessage);

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

        if (disconnect.isDebug()) {
            Logger.debug("Sending DISCONNECT");
        }

        if (disconnect.isVerbose()) {
            Logger.trace("Sending DISCONNECT with Mqtt3Disconnect: {}", Mqtt3Disconnect.class);
        }

        client.toAsync()
                .disconnect();

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
