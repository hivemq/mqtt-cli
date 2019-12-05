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
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.disconnect.Mqtt3Disconnect;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
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
import com.hivemq.client.util.TypeSwitch;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.encoders.Base64;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    void mqtt5Connect(final @NotNull Mqtt5Client client, final @NotNull Mqtt5Connect connectMessage, final @NotNull Connect connect) {

        String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        if (connect.isVerbose()) {
            Logger.trace("{} sending CONNECT {}",
                    clientLogPrefix,
                    connectMessage);
        }
        else if (connect.isDebug()) {
            Logger.debug("{} sending CONNECT", clientLogPrefix);
        }

        final Mqtt5ConnAck connAck = client
                .toBlocking()
                .connect(connectMessage);

        if (connect.isVerbose()) {
            Logger.trace("{} received CONNACK {} ",  clientLogPrefix, connAck);
        }
        else if (connect.isDebug()) {
            Logger.debug("{} received CONNACK ({})",  clientLogPrefix,
                    connAck.getReasonCode());
        }

    }

    void mqtt3Connect(final @NotNull Mqtt3Client client, final @NotNull Mqtt3Connect connectMessage, final @NotNull Connect connect) {

        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        if (connect.isVerbose()) {
            Logger.trace("{} sending CONNECT {}",
                    clientLogPrefix,
                    connectMessage);
        }
        else if (connect.isDebug()) {
            Logger.debug("{} sending CONNECT", clientLogPrefix);
        }

        final Mqtt3ConnAck connAck = client
                .toBlocking()
                .connect(connectMessage);

        if (connect.isVerbose()) {
            Logger.trace("{} received CONNACK {} ", clientLogPrefix, connAck);
        }
        else if (connect.isDebug()) {
            Logger.debug("{} received CONNACK ({})", clientLogPrefix,
                    connAck.getReturnCode());
        }

    }

    void mqtt5Subscribe(final @NotNull Mqtt5Client client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos) {

        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt5SubscribeBuilder.Start.Complete builder = Mqtt5Subscribe.builder()
                .topicFilter(topic)
                .qos(qos);

        if (subscribe.getUserProperties() != null) {
            builder.userProperties(subscribe.getUserProperties());
        }


        final Mqtt5Subscribe subscribeMessage = builder.build();

        if (subscribe.isVerbose()) {
            Logger.trace("{} sending SUBSCRIBE {}",
                    clientLogPrefix,
                    subscribeMessage);
        }
        else if (subscribe.isDebug()) {
            Logger.debug("{} sending SUBSCRIBE (Topic: {}, QoS: '{}')",
                    clientLogPrefix,
                    topic,
                    qos);
        }


        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt5PublishCallback(subscribe))
                .whenComplete((subAck, throwable) -> {

                    if (throwable != null) {
                        if (subscribe.isVerbose()) {
                            Logger.trace("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable);
                        }
                        else if (subscribe.isDebug()) {
                            Logger.debug("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable.getMessage());
                        }

                        Logger.error("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                MqttUtils.getRootCause(throwable).getMessage());
                    } else {

                        final String clientKey = MqttUtils.buildKey(client.getConfig().getClientIdentifier().get().toString(),
                                client.getConfig().getServerHost());

                        getClientDataMap().get(clientKey).addSubscription(MqttTopicFilter.of(topic));

                        if (subscribe.isVerbose()) {
                            Logger.trace("{} received SUBACK {}",
                                    clientLogPrefix,
                                    subAck);
                        }
                        else if (subscribe.isDebug()) {
                            Logger.debug("{} received SUBACK ({})",
                                    clientLogPrefix,
                                    subAck.getReasonCodes());
                        }

                    }

                })
        .join();

    }

    void mqtt3Subscribe(final @NotNull Mqtt3Client client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos) {

        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt3SubscribeBuilder.Start.Complete builder = Mqtt3Subscribe.builder()
                .topicFilter(topic)
                .qos(qos);


        final Mqtt3Subscribe subscribeMessage = builder.build();

        if (subscribe.isVerbose()) {
            Logger.trace("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);
        }
        else if (subscribe.isDebug()) {
            Logger.debug("{} sending SUBSCRIBE (Topic: {}, QoS: '{}')", clientLogPrefix, topic, qos);
        }


        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt3PublishCallback(subscribe))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                        if (subscribe.isVerbose()) {
                            Logger.trace("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable);
                        }
                        else if (subscribe.isDebug()) {
                            Logger.debug("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable.getMessage());
                        }

                        Logger.error("{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                MqttUtils.getRootCause(throwable).getMessage());
                    } else {

                        final String clientKey = MqttUtils.buildKey(client.getConfig().getClientIdentifier().get().toString(),
                                client.getConfig().getServerHost());

                        getClientDataMap().get(clientKey).addSubscription(MqttTopicFilter.of(topic));

                        if (subscribe.isVerbose()) {
                            Logger.trace("{} received SUBACK {}", clientLogPrefix, subAck);
                        }
                        else if (subscribe.isDebug()) {
                            Logger.debug("{} received SUBACK ({})", clientLogPrefix, subAck.getReturnCodes());
                        }

                    }
                })
        .join();
    }

    void mqtt5Publish(final @NotNull Mqtt5Client client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos) {

        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

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
            Logger.trace("{} sending PUBLISH {}", clientLogPrefix, publishMessage);
        }
        else if (publish.isDebug()) {
            Logger.debug("{} sending PUBLISH (Topic: '{}', QoS: '{}', Message: '{}')",
                    clientLogPrefix,
                    topic,
                    qos,
                    bufferToString(publish.getMessage()));
        }


        client.toAsync()
            .publish(publishMessage)
            .whenComplete((publishResult, throwable) -> {
                if (throwable != null) {
                    if (publish.isVerbose()) {
                        Logger.trace("{} failed PUBLISH to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                throwable);
                    }
                    else if (publish.isDebug()) {
                        Logger.debug("{} failed PUBLISH to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                throwable.getMessage());
                    }
                    Logger.error("{} failed PUBLISH to TOPIC '{}': {}",
                            clientLogPrefix,
                            topic,
                            MqttUtils.getRootCause(throwable).getMessage());
                }
                else {

                    if (publish.isVerbose()) {
                        Logger.trace("{} received PUBLISH acknowledgement for PUBLISH to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                publishResult);
                    }
                    else if (publish.isDebug()) {

                        TypeSwitch.when(publishResult)
                                .is(Mqtt5PublishResult.Mqtt5Qos1Result.class, qos1Result -> {
                                    Logger.debug("{} received PUBACK for PUBLISH to TOPIC '{}': {}",
                                            clientLogPrefix,
                                            topic,
                                            qos1Result.getPubAck());
                                })
                                .is(Mqtt5PublishResult.Mqtt5Qos2Result.class, qos2Result -> {
                                    Logger.debug("{} received PUBREC for PUBLISH to TOPIC '{}': {}",
                                            clientLogPrefix,
                                            topic,
                                            qos2Result.getPubRec());
                                })
                                .is(Mqtt5PublishResult.Mqtt5Qos2CompleteResult.class, qos2CompleteResult -> {
                                    Logger.debug("{} received PUBCOMP for PUBLISH to TOPIC '{}': {}",
                                            clientLogPrefix,
                                            topic,
                                            qos2CompleteResult.getPubComp());
                                });
                    }
                }
            })
        .join();

    }


    void mqtt3Publish(final @NotNull Mqtt3Client client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt3PublishBuilder.Complete publishBuilder = Mqtt3Publish.builder()
                .topic(topic)
                .qos(qos)
                .payload(publish.getMessage());

        if (publish.getRetain() != null) {
            publishBuilder.retain(publish.getRetain());
        }

        final Mqtt3Publish publishMessage = publishBuilder.build();

        if (publish.isVerbose()) {
            Logger.trace("{} sending PUBLISH {}", clientLogPrefix, publishMessage);
        }
        else if (publish.isDebug()) {
            Logger.debug("{} sending PUBLISH (TOPIC: '{}', QoS: '{}', Message: '{}')",
                    clientLogPrefix,
                    topic,
                    qos,
                    bufferToString(publish.getMessage()));
        }

        client.toAsync().publish(publishMessage)
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {

                        if (publish.isVerbose()) {
                            Logger.trace("{} failed PUBLISH to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable);
                        }
                        else if (publish.isDebug()) {
                            Logger.debug("{} failed PUBLISH to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    throwable.getMessage());
                        }
                        Logger.error("{} failed PUBLISH to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                MqttUtils.getRootCause(throwable).getMessage());

                    } else {

                        if (publish.isVerbose()) {
                            Logger.trace("{} received PUBLISH acknowledgement for PUBLISH to TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    publishResult);
                        }
                        else if (publish.isDebug()) {
                            if (publishResult.getQos().equals(MqttQos.AT_LEAST_ONCE)) {
                                Logger.debug("{} received PUBACK for PUBLISH to TOPIC '{}'",
                                        clientLogPrefix,
                                        topic);
                            }
                            else if (publishResult.getQos().equals(MqttQos.EXACTLY_ONCE)) {
                                Logger.debug("{} received PUBREC for PUBLISH to TOPIC '{}'",
                                        clientLogPrefix,
                                        topic);
                            }
                        }
                    }
                })
        .join();
    }

    @Override
    void mqtt5Unsubscribe(@NotNull final Mqtt5Client client, @NotNull final Unsubscribe unsubscribe) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        for (String topic : unsubscribe.getTopics()) {

            final Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder()
                    .topicFilter(topic)
                    .build();

            if (unsubscribe.isVerbose()) {
                Logger.trace("{} sending UNSUBSCRIBE {}",
                        clientLogPrefix,
                        unsubscribeMessage);
            }
            else if (unsubscribe.isDebug()) {
                Logger.debug("{} sending UNSUBSCRIBE (TOPIC: '{}', userProperties: {})",
                        clientLogPrefix,
                        topic,
                        unsubscribe.getUserProperties());
            }

            client.toAsync()
                    .unsubscribe(unsubscribeMessage)
                    .whenComplete((Mqtt5UnsubAck unsubAck, Throwable throwable) -> {

                        if (throwable != null) {


                            if (unsubscribe.isVerbose()) {
                                Logger.trace("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                        clientLogPrefix,
                                        topic,
                                        throwable);
                            }
                            else if (unsubscribe.isDebug()) {
                                Logger.debug("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                        clientLogPrefix,
                                        topic,
                                        throwable.getMessage());
                            }
                            Logger.error("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    MqttUtils.getRootCause(throwable).getMessage());
                        } else {

                            getClientDataMap().get(unsubscribe.getKey()).removeSubscription(MqttTopicFilter.of(topic));

                            if (unsubscribe.isVerbose()) {
                                Logger.trace("{} received UNSUBACK {}",
                                        clientLogPrefix,
                                        unsubAck);
                            }
                            else if (unsubscribe.isDebug()) {
                                Logger.debug("{} received UNSUBACK ({})",
                                        clientLogPrefix,
                                        unsubAck.getReasonCodes());
                            }

                        }
                    })
            .join();
        }
    }

    @Override
    void mqtt3Unsubscribe(@NotNull final Mqtt3Client client, @NotNull final Unsubscribe unsubscribe) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        for (String topic : unsubscribe.getTopics()) {

            final Mqtt3Unsubscribe unsubscribeMessage = Mqtt3Unsubscribe.builder()
                    .topicFilter(topic)
                    .build();

            if (unsubscribe.isVerbose()) {
                Logger.trace("{} Sending UNSUBSCRIBE {}",
                        clientLogPrefix,
                        unsubscribeMessage);
            }
            else if (unsubscribe.isDebug()) {
                Logger.debug("{} Sending UNSUBSCRIBE (TOPIC: '{}', userProperties: {})",
                        clientLogPrefix,
                        topic,
                        unsubscribe.getUserProperties());
            }

            client.toAsync()
                    .unsubscribe(unsubscribeMessage)
                    .whenComplete((Void unsubAck, Throwable throwable) -> {

                        if (throwable != null) {
                            if (unsubscribe.isVerbose()) {
                                Logger.trace("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                        clientLogPrefix,
                                        topic,
                                        throwable);
                            }
                            else if (unsubscribe.isDebug()) {
                                Logger.debug("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                        clientLogPrefix,
                                        topic,
                                        throwable.getMessage());
                            }
                            Logger.error("{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    MqttUtils.getRootCause(throwable).getMessage());

                        } else {

                            getClientDataMap().get(unsubscribe.getKey()).removeSubscription(MqttTopicFilter.of(topic));

                            if (unsubscribe.isVerbose()) {
                                Logger.trace("{} received UNSUBACK", clientLogPrefix );
                            }
                            else if (unsubscribe.isDebug()) {
                                Logger.debug("{} received UNSUBACK", clientLogPrefix);
                            }

                        }
                    })
            .join();
        }
    }

    @Override
    void mqtt5Disconnect(@NotNull final Mqtt5Client client, @NotNull final Disconnect disconnect) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());


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
            Logger.trace("{} sending DISCONNECT {}", clientLogPrefix, disconnectMessage);
        }
        else if (disconnect.isDebug()) {
            Logger.debug("{} sending DISCONNECT (Reason: {}, SessionExpiryInterval: {}, UserProperties: {})",
                    clientLogPrefix,
                    disconnect.getReasonString(),
                    disconnect.getSessionExpiryInterval(),
                    disconnect.getUserProperties());
        }

        client.toBlocking()
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

        if (disconnect.isVerbose()) {
            Logger.trace("Sending DISCONNECT with Mqtt3Disconnect: {}", Mqtt3Disconnect.class);
        }
        else if (disconnect.isDebug()) {
            Logger.debug("{} Sending DISCONNECT");
        }

        client.toBlocking()
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

    private void logFailedPublish(final @NotNull Throwable t,
                                  final @NotNull String topic,
                                  final @NotNull String clientLogPrefix) {


    }

    private @NotNull String bufferToString(ByteBuffer b) {
        return new String(b.array(), StandardCharsets.UTF_8);
    }
}
