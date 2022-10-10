/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.cli.mqtt;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.*;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {}

    void mqtt5Connect(
            final @NotNull Mqtt5Client client,
            final @NotNull Mqtt5Connect connectMessage,
            final @NotNull Connect connect) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connectMessage);

        final Mqtt5ConnAck connAck = client.toBlocking().connect(connectMessage);

        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    void mqtt3Connect(
            final @NotNull Mqtt3Client client,
            final @NotNull Mqtt3Connect connectMessage,
            final @NotNull Connect connect) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connectMessage);

        final Mqtt3ConnAck connAck = client.toBlocking().connect(connectMessage);

        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    void mqtt5Subscribe(
            final @NotNull Mqtt5Client client,
            final @NotNull Subscribe subscribe,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt5SubscribeBuilder.Start.Complete builder = Mqtt5Subscribe.builder().topicFilter(topic).qos(qos);

        if (subscribe.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            builder.userProperties(subscribe.getUserProperties());
        }

        final Mqtt5Subscribe subscribeMessage = builder.build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt5PublishCallback(subscribe, client))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable,
                                "{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                Throwables.getRootCause(throwable).getMessage());
                    } else {
                        final String clientKey = MqttUtils.buildKey(client.getConfig()
                                        .getClientIdentifier()
                                        .map(Object::toString)
                                        .orElse(""),
                                client.getConfig().getServerHost());

                        getClientDataMap().get(clientKey).addSubscription(MqttTopicFilter.of(topic));

                        Logger.debug("{} received SUBACK {}", clientLogPrefix, subAck);
                    }
                })
                .join();
    }

    void mqtt3Subscribe(
            final @NotNull Mqtt3Client client,
            final @NotNull Subscribe subscribe,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt3SubscribeBuilder.Start.Complete builder = Mqtt3Subscribe.builder().topicFilter(topic).qos(qos);
        final Mqtt3Subscribe subscribeMessage = builder.build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt3PublishCallback(subscribe, client))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable,
                                "{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                Throwables.getRootCause(throwable).getMessage());
                    } else {
                        final String clientKey = MqttUtils.buildKey(client.getConfig()
                                        .getClientIdentifier()
                                        .map(Object::toString)
                                        .orElse(""),
                                client.getConfig().getServerHost());

                        getClientDataMap().get(clientKey).addSubscription(MqttTopicFilter.of(topic));

                        Logger.debug("{} received SUBACK {}", clientLogPrefix, subAck);
                    }
                })
                .join();
    }

    void mqtt5Publish(
            final @NotNull Mqtt5Client client,
            final @NotNull Publish publish,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
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
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.retain(publish.getRetain());
        }
        if (publish.getMessageExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.messageExpiryInterval(publish.getMessageExpiryInterval());
        }
        if (publish.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.userProperties(publish.getUserProperties());
        }

        final Mqtt5Publish publishMessage = publishBuilder.build();

        Logger.debug("{} sending PUBLISH ('{}') {}",
                clientLogPrefix,
                bufferToString(publish.getMessage()),
                publishMessage);

        client.toAsync().publish(publishMessage).whenComplete((publishResult, throwable) -> {
            if (throwable != null) {
                Logger.error(throwable,
                        "{} failed PUBLISH to TOPIC '{}': {}",
                        clientLogPrefix,
                        topic,
                        Throwables.getRootCause(throwable).getMessage());
            } else {
                Logger.debug("{} received PUBLISH acknowledgement {}", clientLogPrefix, publishResult);
            }
        }).join();
    }

    void mqtt3Publish(
            final @NotNull Mqtt3Client client,
            final @NotNull Publish publish,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt3PublishBuilder.Complete publishBuilder =
                Mqtt3Publish.builder().topic(topic).qos(qos).payload(publish.getMessage());

        if (publish.getRetain() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.retain(publish.getRetain());
        }

        final Mqtt3Publish publishMessage = publishBuilder.build();

        Logger.debug("{} sending PUBLISH ('{}') {}",
                clientLogPrefix,
                bufferToString(publish.getMessage()),
                publishMessage);

        client.toAsync().publish(publishMessage).whenComplete((publishResult, throwable) -> {
            if (throwable != null) {
                Logger.error(throwable,
                        "{} failed PUBLISH to TOPIC '{}': {}",
                        clientLogPrefix,
                        topic,
                        Throwables.getRootCause(throwable).getMessage());
            } else {
                Logger.debug("{} received PUBLISH acknowledgement {}", clientLogPrefix, publishResult);
            }
        }).join();
    }

    @Override
    void mqtt5Unsubscribe(final @NotNull Mqtt5Client client, final @NotNull Unsubscribe unsubscribe) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt5UserProperties userProperties = unsubscribe.getUserProperties() != null? unsubscribe.getUserProperties() : Mqtt5UserProperties.of();

        for (final String topic : unsubscribe.getTopics()) {
            final Mqtt5Unsubscribe unsubscribeMessage =
                    Mqtt5Unsubscribe.builder().topicFilter(topic).userProperties(userProperties).build();

            Logger.debug("{} sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

            client.toAsync()
                    .unsubscribe(unsubscribeMessage)
                    .whenComplete((Mqtt5UnsubAck unsubAck, Throwable throwable) -> {
                        if (throwable != null) {

                            Logger.error(throwable,
                                    "{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                                    clientLogPrefix,
                                    topic,
                                    Throwables.getRootCause(throwable).getMessage());
                        } else {
                            getClientDataMap().get(unsubscribe.getKey()).removeSubscription(MqttTopicFilter.of(topic));

                            Logger.debug("{} received UNSUBACK {}", clientLogPrefix, unsubAck);
                        }
                    })
                    .join();
        }
    }

    @Override
    void mqtt3Unsubscribe(final @NotNull Mqtt3Client client, final @NotNull Unsubscribe unsubscribe) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        for (final String topic : unsubscribe.getTopics()) {
            final Mqtt3Unsubscribe unsubscribeMessage = Mqtt3Unsubscribe.builder().topicFilter(topic).build();

            Logger.debug("{} Sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

            client.toAsync().unsubscribe(unsubscribeMessage).whenComplete((Void unsubAck, Throwable throwable) -> {
                if (throwable != null) {
                    Logger.error(throwable,
                            "{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                            clientLogPrefix,
                            topic,
                            Throwables.getRootCause(throwable).getMessage());
                } else {
                    getClientDataMap().get(unsubscribe.getKey()).removeSubscription(MqttTopicFilter.of(topic));
                    Logger.debug("{} received UNSUBACK", clientLogPrefix);
                }
            }).join();
        }
    }

    @Override
    void mqtt5Disconnect(final @NotNull Mqtt5Client client, final @NotNull Disconnect disconnect) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt5DisconnectBuilder disconnectBuilder = Mqtt5Disconnect.builder();

        if (disconnect.getReasonString() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.reasonString(disconnect.getReasonString());
        }
        if (disconnect.getSessionExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.sessionExpiryInterval(disconnect.getSessionExpiryInterval());
        }
        if (disconnect.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.userProperties(disconnect.getUserProperties());
        }

        final Mqtt5Disconnect disconnectMessage = disconnectBuilder.build();

        Logger.debug("{} sending DISCONNECT {}", clientLogPrefix, disconnectMessage);

        client.toBlocking().disconnect(disconnectMessage);
    }

    @Override
    void mqtt3Disconnect(final @NotNull Mqtt3Client client, final @NotNull Disconnect disconnect) {
        Logger.debug("{} sending DISCONNECT", LoggerUtils.getClientPrefix(client.getConfig()));

        client.toBlocking().disconnect();
    }

    private @NotNull String bufferToString(final @NotNull ByteBuffer b) {
        return new String(b.array(), StandardCharsets.UTF_8);
    }
}
