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
package com.hivemq.cli.mqtt.clients.mqtt5;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.ConnectRestrictionOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.CliMqttClient;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt5PublishCallback;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictionsBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CliMqtt5Client implements CliMqttClient {

    private final @NotNull Mqtt5Client delegate;
    private final @NotNull List<Mqtt5Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private @Nullable LocalDateTime connectedTime;

    public CliMqtt5Client(final @NotNull Mqtt5Client delegate) {
        this.delegate = delegate;
    }

    public void connect(final @NotNull ConnectOptions connectOptions) {
        final Mqtt5Connect connect = buildConnect(connectOptions);
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());
        Logger.debug("{} sending CONNECT {}", clientLogPrefix, connect);
        final Mqtt5ConnAck connAck = delegate.toBlocking().connect(connect);
        this.connectedTime = LocalDateTime.now();
        Logger.debug("{} received CONNACK {} ", clientLogPrefix, connAck);
    }

    @Override
    public void publish(final @NotNull PublishOptions publishOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());

        for (int i = 0; i < publishOptions.getTopics().length; i++) {
            final String topic = publishOptions.getTopics()[i];
            final MqttQos qos = publishOptions.getQos()[i];

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
                    new String(publishOptions.getMessage().array(), StandardCharsets.UTF_8),
                    publishMessage);

            delegate.toAsync().publish(publishMessage).whenComplete((publishResult, throwable) -> {
                if (throwable != null) {
                    Logger.error(throwable,
                            "{} failed PUBLISH to topic '{}': {}",
                            clientLogPrefix,
                            topic,
                            Throwables.getRootCause(throwable).getMessage());
                } else {
                    Logger.debug("{} received PUBLISH acknowledgement {}", clientLogPrefix, publishResult);
                }
            }).join();
        }
    }

    @Override
    public void subscribe(final @NotNull SubscribeOptions subscribeOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());

        final String[] topics = subscribeOptions.getTopics();
        final MqttQos[] qos = subscribeOptions.getQos();
        final ArrayList<Mqtt5Subscription> subscriptions = new ArrayList<>(topics.length);
        for (int i = 0; i < topics.length; i++) {
            final Mqtt5Subscription subscription =
                    Mqtt5Subscription.builder().topicFilter(topics[i]).qos(qos[i]).build();
            subscriptions.add(subscription);
        }

        final Mqtt5SubscribeBuilder.Complete builder = Mqtt5Subscribe.builder().addSubscriptions(subscriptions);

        if (subscribeOptions.getUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            builder.userProperties(subscribeOptions.getUserProperties());
        }

        final Mqtt5Subscribe subscribeMessage = builder.build();

        Logger.debug("{} sending SUBSCRIBE {}", clientLogPrefix, subscribeMessage);

        delegate.toAsync()
                .subscribe(subscribeMessage, new SubscribeMqtt5PublishCallback(subscribeOptions, delegate))
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

        final Mqtt5Unsubscribe unsubscribeMessage = Mqtt5Unsubscribe.builder()
                .addTopicFilters(topicFilters)
                .userProperties(unsubscribeOptions.getUserProperties())
                .build();

        Logger.debug("{} sending UNSUBSCRIBE {}", clientLogPrefix, unsubscribeMessage);

        delegate.toAsync().unsubscribe(unsubscribeMessage).whenComplete((unsubAck, throwable) -> {
            if (throwable != null) {
                Logger.error(throwable,
                        "{} failed UNSUBSCRIBE from topic(s) '[{}]': {}",
                        clientLogPrefix,
                        Arrays.toString(unsubscribeOptions.getTopics()),
                        Throwables.getRootCause(throwable).getMessage());
            } else {
                subscriptions.removeIf(sub -> topicFilters.contains(sub.getTopicFilter()));
                Logger.debug("{} received UNSUBACK {}", clientLogPrefix, unsubAck);
            }
        }).join();
    }

    @Override
    public void disconnect(final @NotNull DisconnectOptions disconnectOptions) {
        final String clientLogPrefix = LoggerUtils.getClientPrefix(delegate.getConfig());
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

        delegate.toBlocking().disconnect(disconnectMessage);
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
        if (connectedTime == null) {
            // TODO
            connectedTime = LocalDateTime.now();
        }
        return connectedTime;
    }

    @Override
    public @NotNull MqttClientState getState() {
        return delegate.getState();
    }

    @Override
    public @NotNull String getSslProtocols() {
        return delegate.getConfig()
                .getSslConfig()
                .map(MqttClientSslConfig::getProtocols)
                .map(Optional::toString)
                .orElse("NO_SSL");
    }

    @Override
    public int getServerPort() {
        return delegate.getConfig().getServerPort();
    }

    @Override
    public @NotNull List<MqttTopicFilter> getSubscribedTopics() {
        return subscriptions.stream().map(Mqtt5Subscription::getTopicFilter).collect(Collectors.toList());

    }

    private static @NotNull Mqtt5Connect buildConnect(final @NotNull ConnectOptions connectOptions) {
        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder();

        final Mqtt5ConnectRestrictions restrictions = buildRestrictions(connectOptions.getConnectRestrictionOptions());
        if (restrictions != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.restrictions(restrictions);
        }

        if (connectOptions.getCleanStart() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.cleanStart(connectOptions.getCleanStart());
        }
        if (connectOptions.getKeepAlive() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.keepAlive(connectOptions.getKeepAlive());
        }
        if (connectOptions.getSessionExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.sessionExpiryInterval(connectOptions.getSessionExpiryInterval());
        }
        if (connectOptions.getConnectUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.userProperties(connectOptions.getConnectUserProperties());
        }

        //noinspection ResultOfMethodCallIgnored
        connectBuilder.simpleAuth(buildMqtt5Authentication(connectOptions.getAuthenticationOptions()));

        return connectBuilder.build();
    }

    private static @Nullable Mqtt5ConnectRestrictions buildRestrictions(final @NotNull ConnectRestrictionOptions connectRestrictionOptions) {
        final Mqtt5ConnectRestrictionsBuilder restrictionsBuilder = Mqtt5ConnectRestrictions.builder();

        if (connectRestrictionOptions.getReceiveMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.receiveMaximum(connectRestrictionOptions.getReceiveMaximum());
        }
        if (connectRestrictionOptions.getSendMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendMaximum(connectRestrictionOptions.getSendMaximum());
        }
        if (connectRestrictionOptions.getMaximumPacketSize() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.maximumPacketSize(connectRestrictionOptions.getMaximumPacketSize());
        }
        if (connectRestrictionOptions.getSendMaximumPacketSize() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendMaximumPacketSize(connectRestrictionOptions.getSendMaximumPacketSize());
        }
        if (connectRestrictionOptions.getTopicAliasMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.topicAliasMaximum(connectRestrictionOptions.getTopicAliasMaximum());
        }
        if (connectRestrictionOptions.getSendTopicAliasMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendTopicAliasMaximum(connectRestrictionOptions.getSendTopicAliasMaximum());
        }
        if (connectRestrictionOptions.getRequestProblemInformation() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.requestProblemInformation(connectRestrictionOptions.getRequestProblemInformation());
        }
        if (connectRestrictionOptions.getRequestResponseInformation() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.requestResponseInformation(connectRestrictionOptions.getRequestResponseInformation());
        }

        final Mqtt5ConnectRestrictions restrictions = restrictionsBuilder.build();

        // Workaround : if the built connect restrictions are the default ones do not append them to the connect builder
        // -> Else the connectMessage.toString() method will flood the logging output
        if (!restrictions.equals(Mqtt5ConnectRestrictions.builder().build())) {
            return restrictions;
        } else {
            return null;
        }
    }

    private static @Nullable Mqtt5SimpleAuth buildMqtt5Authentication(final @NotNull AuthenticationOptions authenticationOptions) {
        if (authenticationOptions.getUser() != null && authenticationOptions.getPassword() != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(authenticationOptions.getUser())
                    .password(authenticationOptions.getPassword())
                    .build();
        } else if (authenticationOptions.getPassword() != null) {
            return Mqtt5SimpleAuth.builder().password(authenticationOptions.getPassword()).build();
        } else if (authenticationOptions.getUser() != null) {
            return Mqtt5SimpleAuth.builder().username(authenticationOptions.getUser()).build();
        }
        return null;
    }

}
