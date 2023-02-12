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
package com.hivemq.cli.mqtt.clients.mqtt3;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.CliMqttClient;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt3PublishCallback;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CliMqtt3Client implements CliMqttClient {

    private final @NotNull Mqtt3Client delegate;
    private final @NotNull List<Mqtt3Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private @Nullable LocalDateTime connectedAt;

    public CliMqtt3Client(final @NotNull Mqtt3Client delegate) {
        this.delegate = delegate;
    }

    public void connect(final @NotNull ConnectOptions connectOptions) {
        final Mqtt3Connect connect = buildConnect(connectOptions);
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());
        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connect);
        final Mqtt3ConnAck connAck = delegate.toBlocking().connect(connect);
        this.connectedAt = LocalDateTime.now();
        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    @Override
    public void publish(final @NotNull PublishOptions publishOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());
        final ArrayList<CompletableFuture<Mqtt3Publish>> publishFutures = new ArrayList<>();

        final int topicsSize = publishOptions.getTopics().length;
        final int qosSize = publishOptions.getQos().length;
        if (topicsSize != qosSize) {
            throw new IllegalArgumentException(String.format("Topics size (%d) does not match QoS size (%d)",
                    topicsSize,
                    qosSize));
        }

        for (int i = 0; i < topicsSize; i++) {
            final String topic = publishOptions.getTopics()[i];
            final MqttQos qos = publishOptions.getQos()[i];

            final Mqtt3PublishBuilder.Complete publishBuilder =
                    Mqtt3Publish.builder().topic(topic).qos(qos).payload(publishOptions.getMessage());

            if (publishOptions.getRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                publishBuilder.retain(publishOptions.getRetain());
            }

            final Mqtt3Publish publishMessage = publishBuilder.build();

            Logger.debug("{} sending PUBLISH ('{}') {}",
                    clientLogPrefix,
                    new String(publishOptions.getMessage().array(), StandardCharsets.UTF_8),
                    publishMessage);

            final CompletableFuture<Mqtt3Publish> publishFuture = delegate.toAsync().publish(publishMessage);
            publishFutures.add(publishFuture);
            publishFuture.whenComplete((publishResult, throwable) -> {
                if (throwable != null) {
                    Logger.error(throwable,
                            "{} failed PUBLISH to topic '{}': {}",
                            clientLogPrefix,
                            topic,
                            Throwables.getRootCause(throwable).getMessage());
                } else {
                    Logger.debug("{} received PUBLISH acknowledgement {}", clientLogPrefix, publishResult);
                }
            });
        }

        CompletableFuture.allOf(publishFutures.toArray(new CompletableFuture[]{})).join();
    }

    @Override
    public void subscribe(final @NotNull SubscribeOptions subscribeOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());

        final String[] topics = subscribeOptions.getTopics();
        final MqttQos[] qos = subscribeOptions.getQos();
        if (topics.length != qos.length) {
            throw new IllegalArgumentException(String.format("Topics size (%d) does not match QoS size (%d)",
                    topics.length,
                    qos.length));
        }
        final ArrayList<Mqtt3Subscription> subscriptions = new ArrayList<>();
        for (int i = 0; i < topics.length; i++) {
            final Mqtt3Subscription subscription =
                    Mqtt3Subscription.builder().topicFilter(topics[i]).qos(qos[i]).build();
            subscriptions.add(subscription);
        }

        final Mqtt3Subscribe subscribeMessage = Mqtt3Subscribe.builder().addSubscriptions(subscriptions).build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        delegate.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt3PublishCallback(subscribeOptions, delegate))
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Logger.error(throwable,
                                "{} failed SUBSCRIBE to topic(s) '{}': {}",
                                clientLogPrefix,
                                Arrays.toString(topics),
                                Throwables.getRootCause(throwable).getMessage());
                    } else {
                        this.subscriptions.addAll(subscriptions);
                        Logger.debug("{} received SUBACK {}", clientLogPrefix, subAck);
                    }
                })
                .join();
    }

    @Override
    public void unsubscribe(final @NotNull UnsubscribeOptions unsubscribeOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());

        final List<MqttTopicFilter> topicFilters =
                Arrays.stream(unsubscribeOptions.getTopics()).map(MqttTopicFilter::of).collect(Collectors.toList());

        final Mqtt3Unsubscribe unsubscribeMessage = Mqtt3Unsubscribe.builder().addTopicFilters(topicFilters).build();

        Logger.debug("{} sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

        delegate.toAsync().unsubscribe(unsubscribeMessage).whenComplete((unsubAck, throwable) -> {
            if (throwable != null) {
                Logger.error(throwable,
                        "{} failed UNSUBSCRIBE from topic(s) '{}': {}",
                        clientLogPrefix,
                        Arrays.toString(unsubscribeOptions.getTopics()),
                        Throwables.getRootCause(throwable).getMessage());
            } else {
                subscriptions.removeIf(sub -> topicFilters.contains(sub.getTopicFilter()));
                Logger.debug("{} received UNSUBACK", clientLogPrefix);
            }
        }).join();
    }

    @Override
    public void disconnect(final @NotNull DisconnectOptions disconnectOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());
        Logger.debug("{} sending DISCONNECT", clientLogPrefix);
        delegate.toAsync().disconnect().whenComplete((result, throwable) -> {
            if (throwable != null) {
                Logger.error(throwable,
                        "{} failed to DISCONNECT gracefully: {}",
                        clientLogPrefix,
                        Throwables.getRootCause(throwable).getMessage());
            } else {
                Logger.debug("{} disconnected successfully", clientLogPrefix);
            }
        }).join();
    }

    @Override
    public boolean isConnected() {
        return delegate.getState().isConnected();
    }

    @Override
    public @NotNull String getClientIdentifier() {
        return delegate.getConfig().getClientIdentifier().map(Object::toString).orElse("UNKNOWN");
    }

    @Override
    public @NotNull String getServerHost() {
        return delegate.getConfig().getServerHost();
    }

    @Override
    public @NotNull MqttVersion getMqttVersion() {
        return delegate.getConfig().getMqttVersion();
    }

    @Override
    public @NotNull LocalDateTime getConnectedAt() {
        if (connectedAt == null) {
            throw new IllegalStateException("connectedAt must not be null after a client has connected successfully");
        }
        return connectedAt;
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull String getSslProtocols() {
        final Optional<MqttClientSslConfig> sslConfig = delegate.getConfig().getSslConfig();
        if (sslConfig.isPresent()) {
            final Optional<List<String>> protocols = sslConfig.get().getProtocols();
            return protocols.orElse(ImmutableList.of()).toString();
        } else {
            return "NO_SSL";
        }
    }

    @Override
    public int getServerPort() {
        return delegate.getConfig().getServerPort();
    }

    @Override
    public @NotNull List<MqttTopicFilter> getSubscribedTopics() {
        return subscriptions.stream().map(Mqtt3Subscription::getTopicFilter).collect(Collectors.toList());
    }

    public @NotNull Mqtt3Client getDelegate() {
        return delegate;
    }

    private static @NotNull Mqtt3Connect buildConnect(final @NotNull ConnectOptions connectOptions) {
        final Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder();

        if (connectOptions.getCleanStart() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.cleanSession(connectOptions.getCleanStart());
        }
        if (connectOptions.getKeepAlive() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.keepAlive(connectOptions.getKeepAlive());
        }

        //noinspection ResultOfMethodCallIgnored
        connectBuilder.simpleAuth(buildMqtt3Authentication(connectOptions.getAuthenticationOptions()));

        return connectBuilder.build();
    }

    private static @Nullable Mqtt3SimpleAuth buildMqtt3Authentication(final @NotNull AuthenticationOptions authenticationOptions) {
        if (authenticationOptions.getUser() != null && authenticationOptions.getPassword() != null) {
            return Mqtt3SimpleAuth.builder()
                    .username(authenticationOptions.getUser())
                    .password(authenticationOptions.getPassword())
                    .build();
        } else if (authenticationOptions.getUser() != null) {
            return Mqtt3SimpleAuth.builder().username(authenticationOptions.getUser()).build();
        } else if (authenticationOptions.getPassword() != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
        return null;
    }
}
