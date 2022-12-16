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
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.utils.LoggerUtils;
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
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    void mqtt5Connect(
            final @NotNull Mqtt5Client client, final @NotNull Mqtt5Connect connectMessage) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connectMessage);

        final Mqtt5ConnAck connAck = client.toBlocking().connect(connectMessage);

        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    void mqtt3Connect(
            final @NotNull Mqtt3Client client, final @NotNull Mqtt3Connect connectMessage) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connectMessage);

        final Mqtt3ConnAck connAck = client.toBlocking().connect(connectMessage);

        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    void mqtt5Subscribe(
            final @NotNull Mqtt5Client client,
            final @NotNull SubscribeOptions subscribeOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt5SubscribeBuilder.Start.Complete builder = Mqtt5Subscribe.builder().topicFilter(topic).qos(qos);

        if (subscribeOptions.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            builder.userProperties(subscribeOptions.getUserProperties());
        }

        final Mqtt5Subscribe subscribeMessage = builder.build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt5PublishCallback(subscribeOptions, client))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable,
                                "{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                Throwables.getRootCause(throwable).getMessage());
                    } else {
                        final ClientKey clientKey = ClientKey.of(client);
                        getClientDataMap().get(clientKey).addSubscription(MqttTopicFilter.of(topic));
                        Logger.debug("{} received SUBACK {}", clientLogPrefix, subAck);
                    }
                })
                .join();
    }

    void mqtt3Subscribe(
            final @NotNull Mqtt3Client client,
            final @NotNull SubscribeOptions subscribeOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt3SubscribeBuilder.Start.Complete builder = Mqtt3Subscribe.builder().topicFilter(topic).qos(qos);
        final Mqtt3Subscribe subscribeMessage = builder.build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        client.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt3PublishCallback(subscribeOptions, client))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable,
                                "{} failed SUBSCRIBE to TOPIC '{}': {}",
                                clientLogPrefix,
                                topic,
                                Throwables.getRootCause(throwable).getMessage());
                    } else {
                        getClientDataMap().get(ClientKey.of(client)).addSubscription(MqttTopicFilter.of(topic));

                        Logger.debug("{} received SUBACK {}", clientLogPrefix, subAck);
                    }
                })
                .join();
    }

    void mqtt5Publish(
            final @NotNull Mqtt5Client client,
            final @NotNull PublishOptions publishOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt5PublishBuilder.Complete publishBuilder = Mqtt5Publish.builder()
                .topic(topic)
                .qos(qos)
                .payload(publishOptions.getMessage())
                .payloadFormatIndicator(publishOptions.getPayloadFormatIndicator())
                .contentType(publishOptions.getContentType())
                .responseTopic(publishOptions.getResponseTopic())
                .correlationData(publishOptions.getCorrelationData());

        if (publishOptions.getRetain() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.retain(publishOptions.getRetain());
        }
        if (publishOptions.getMessageExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.messageExpiryInterval(publishOptions.getMessageExpiryInterval());
        }
        if (publishOptions.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.userProperties(publishOptions.getUserProperties());
        }

        final Mqtt5Publish publishMessage = publishBuilder.build();

        Logger.debug("{} sending PUBLISH ('{}') {}",
                clientLogPrefix,
                bufferToString(publishOptions.getMessage()),
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
            final @NotNull PublishOptions publishOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        final Mqtt3PublishBuilder.Complete publishBuilder =
                Mqtt3Publish.builder().topic(topic).qos(qos).payload(publishOptions.getMessage());

        if (publishOptions.getRetain() != null) {
            //noinspection ResultOfMethodCallIgnored
            publishBuilder.retain(publishOptions.getRetain());
        }

        final Mqtt3Publish publishMessage = publishBuilder.build();

        Logger.debug("{} sending PUBLISH ('{}') {}",
                clientLogPrefix,
                bufferToString(publishOptions.getMessage()),
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
    void mqtt5Unsubscribe(final @NotNull Mqtt5Client client, final @NotNull UnsubscribeOptions unsubscribeOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        for (final String topic : unsubscribeOptions.getTopics()) {
            final Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder()
                    .topicFilter(topic)
                    .userProperties(unsubscribeOptions.getUserProperties())
                    .build();

            Logger.debug("{} sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

            client.toAsync().unsubscribe(unsubscribeMessage).whenComplete((unsubAck, throwable) -> {
                if (throwable != null) {

                    Logger.error(throwable,
                            "{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                            clientLogPrefix,
                            topic,
                            Throwables.getRootCause(throwable).getMessage());
                } else {
                    getClientDataMap().get(ClientKey.of(client)).removeSubscription(MqttTopicFilter.of(topic));

                    Logger.debug("{} received UNSUBACK {}", clientLogPrefix, unsubAck);
                }
            }).join();
        }
    }

    @Override
    void mqtt3Unsubscribe(final @NotNull Mqtt3Client client, final @NotNull UnsubscribeOptions unsubscribeOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());

        for (final String topic : unsubscribeOptions.getTopics()) {
            final Mqtt3Unsubscribe unsubscribeMessage = Mqtt3Unsubscribe.builder().topicFilter(topic).build();

            Logger.debug("{} sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

            client.toAsync().unsubscribe(unsubscribeMessage).whenComplete((unsubAck, throwable) -> {
                if (throwable != null) {
                    Logger.error(throwable,
                            "{} failed UNSUBSCRIBE from TOPIC '{}': {}",
                            clientLogPrefix,
                            topic,
                            Throwables.getRootCause(throwable).getMessage());
                } else {
                    getClientDataMap().get(ClientKey.of(client)).removeSubscription(MqttTopicFilter.of(topic));
                    Logger.debug("{} received UNSUBACK", clientLogPrefix);
                }
            }).join();
        }
    }

    @Override
    void mqtt5Disconnect(final @NotNull Mqtt5Client client, final @NotNull DisconnectOptions disconnectOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(client.getConfig());
        final Mqtt5DisconnectBuilder disconnectBuilder = Mqtt5Disconnect.builder();

        if (disconnectOptions.getReasonString() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.reasonString(disconnectOptions.getReasonString());
        }
        if (disconnectOptions.getSessionExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.sessionExpiryInterval(disconnectOptions.getSessionExpiryInterval());
        }
        if (disconnectOptions.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            disconnectBuilder.userProperties(disconnectOptions.getUserProperties());
        }

        final Mqtt5Disconnect disconnectMessage = disconnectBuilder.build();

        Logger.debug("{} sending DISCONNECT {}", clientLogPrefix, disconnectMessage);

        client.toBlocking().disconnect(disconnectMessage);
    }

    @Override
    void mqtt3Disconnect(final @NotNull Mqtt3Client client, final @NotNull DisconnectOptions disconnectOptions) {
        Logger.debug("{} sending DISCONNECT", LoggerUtils.getClientPrefix(client.getConfig()));

        client.toBlocking().disconnect();
    }

    private @NotNull String bufferToString(final @NotNull ByteBuffer b) {
        return new String(b.array(), StandardCharsets.UTF_8);
    }
}
