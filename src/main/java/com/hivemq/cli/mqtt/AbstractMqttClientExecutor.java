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

import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.ConnectRestrictionOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.commands.options.WillOptions;
import com.hivemq.cli.utils.IntersectionUtil;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttSharedTopicFilter;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictionsBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

abstract class AbstractMqttClientExecutor {

    private static final @NotNull Map<ClientKey, ClientData> clientKeyToClientData = new ConcurrentHashMap<>();

    abstract void mqtt5Connect(
            final @NotNull Mqtt5Client client, final @NotNull Mqtt5Connect connectMessage);

    abstract void mqtt3Connect(
            final @NotNull Mqtt3Client client, final @NotNull Mqtt3Connect connectMessage);

    abstract void mqtt5Subscribe(
            final @NotNull Mqtt5Client client,
            final @NotNull SubscribeOptions subscribeOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(
            final @NotNull Mqtt3Client client,
            final @NotNull SubscribeOptions subscribeOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt5Publish(
            final @NotNull Mqtt5Client client,
            final @NotNull PublishOptions publishOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt3Publish(
            final @NotNull Mqtt3Client client,
            final @NotNull PublishOptions publishOptions,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt5Unsubscribe(
            final @NotNull Mqtt5Client client, final @NotNull UnsubscribeOptions unsubscribeOptions);

    abstract void mqtt3Unsubscribe(
            final @NotNull Mqtt3Client client, final @NotNull UnsubscribeOptions unsubscribeOptions);

    abstract void mqtt5Disconnect(
            final @NotNull Mqtt5Client client, final @NotNull DisconnectOptions disconnectOptions);

    abstract void mqtt3Disconnect(
            final @NotNull Mqtt3Client client, final @NotNull DisconnectOptions disconnectOptions);

    public @NotNull MqttClient connect(final @NotNull ConnectOptions connectOptions) throws Exception {
        return connect(connectOptions, null);
    }

    public @NotNull MqttClient connect(
            final @NotNull ConnectOptions connectOptions, final @Nullable SubscribeOptions subscribeOptions)
            throws Exception {

        final ClientKey clientKey = ClientKey.of(connectOptions.getIdentifier(), connectOptions.getHost());
        if (isConnected(clientKey)) {
            Logger.debug("Client is already connected ({})", clientKey);
            Logger.info("Using already connected  ({})", clientKey);
            return clientKeyToClientData.get(clientKey).getClient();
        }

        switch (connectOptions.getVersion()) {
            case MQTT_5_0:
                return connectMqtt5Client(connectOptions, subscribeOptions);
            case MQTT_3_1_1:
                return connectMqtt3Client(connectOptions, subscribeOptions);
            default:
                throw new IllegalStateException("The MQTT Version specified is not supported. Version was " +
                        connectOptions.getVersion());
        }
    }

    public void subscribe(final @NotNull MqttClient client, final @NotNull SubscribeOptions subscribeOptions) {
        for (int i = 0; i < subscribeOptions.getTopics().length; i++) {
            final String topic = subscribeOptions.getTopics()[i];

            // This check only works as subscribes are implemented blocking.
            // Otherwise, we would need to check the topics before they are iterated
            // as they are added to the client data after a successful subscribe.
            final List<MqttTopicFilter> intersectingFilters =
                    checkForSharedTopicDuplicate(clientKeyToClientData.get(ClientKey.of(client)).getSubscribedTopics(),
                            topic);
            // Client{clientIdentifier='hmq_RcrDi_18591249', hostname='broker.hivemq.com'} -> {ClientData@3806}
            // Client{clientIdentifier='hmq_RcrDi_18591249', hostname='broker.hivemq.com'}
            if (!intersectingFilters.isEmpty()) {
                Logger.warn("WARN: New subscription to '{}' intersects with already existing subscription(s) {}",
                        topic,
                        intersectingFilters);
            }

            final int qosI = i < subscribeOptions.getQos().length ? i : subscribeOptions.getQos().length - 1;
            final MqttQos qos = subscribeOptions.getQos()[qosI];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Subscribe((Mqtt5Client) client, subscribeOptions, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Subscribe((Mqtt3Client) client, subscribeOptions, topic, qos);
                    break;
            }
        }
    }

    @VisibleForTesting
    @NotNull List<MqttTopicFilter> checkForSharedTopicDuplicate(
            final @NotNull Set<MqttTopicFilter> subscribedFilters, final @NotNull String topic) {
        final List<MqttTopicFilter> intersectingFilters = new ArrayList<>();
        final MqttTopicFilter newUnidentifiedFilter = MqttTopicFilter.of(topic);

        final MqttTopicFilter newFilter;
        if (newUnidentifiedFilter.isShared()) {
            newFilter = ((MqttSharedTopicFilter) newUnidentifiedFilter).getTopicFilter();
        } else {
            newFilter = newUnidentifiedFilter;
        }

        for (final MqttTopicFilter subscribedFilter : subscribedFilters) {
            final MqttTopicFilter existingFilter;
            if (subscribedFilter.isShared()) {
                existingFilter = ((MqttSharedTopicFilter) subscribedFilter).getTopicFilter();
            } else {
                existingFilter = subscribedFilter;
            }

            if (IntersectionUtil.intersects(existingFilter, newFilter)) {
                intersectingFilters.add(subscribedFilter);
            }
        }
        return intersectingFilters;
    }

    public void publish(final @NotNull MqttClient client, final @NotNull PublishOptions publishOptions) {
        for (int i = 0; i < publishOptions.getTopics().length; i++) {
            final String topic = publishOptions.getTopics()[i];
            final int qosI = i < publishOptions.getQos().length ? i : publishOptions.getQos().length - 1;
            final MqttQos qos = publishOptions.getQos()[qosI];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Publish((Mqtt5Client) client, publishOptions, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Publish((Mqtt3Client) client, publishOptions, topic, qos);
                    break;
            }
        }
    }

    public void disconnect(final @NotNull ClientKey clientKey, final @NotNull DisconnectOptions disconnectOptions) {
        final ClientData clientData = clientKeyToClientData.get(clientKey);
        if (clientData != null) {
            disconnect(clientData.getClient(), disconnectOptions);
        }
    }

    public void disconnect(final @NotNull MqttClient client, final @NotNull DisconnectOptions disconnectOptions) {
        switch (client.getConfig().getMqttVersion()) {
            case MQTT_5_0:
                mqtt5Disconnect((Mqtt5Client) client, disconnectOptions);
                break;
            case MQTT_3_1_1:
                mqtt3Disconnect((Mqtt3Client) client, disconnectOptions);
                break;
        }
        clientKeyToClientData.remove(ClientKey.of(client));
    }

    public void disconnectAllClients(final @NotNull DisconnectOptions disconnectOptions) {
        for (final Map.Entry<ClientKey, ClientData> entry : clientKeyToClientData.entrySet()) {
            final MqttClient client = entry.getValue().getClient();
            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Disconnect((Mqtt5Client) client, disconnectOptions);
                    break;
                case MQTT_3_1_1:
                    mqtt3Disconnect((Mqtt3Client) client, disconnectOptions);
                    break;
            }
        }
        clientKeyToClientData.clear();
    }

    public void unsubscribe(final @NotNull MqttClient client, final @NotNull UnsubscribeOptions unsubscribeOptions) {
        switch (client.getConfig().getMqttVersion()) {
            case MQTT_5_0:
                mqtt5Unsubscribe((Mqtt5Client) client, unsubscribeOptions);
                break;
            case MQTT_3_1_1:
                mqtt3Unsubscribe((Mqtt3Client) client, unsubscribeOptions);
                break;
        }

    }

    public boolean isConnected(final @NotNull ClientKey key) {
        if (clientKeyToClientData.containsKey(key)) {
            final MqttClient client = clientKeyToClientData.get(key).getClient();
            final MqttClientState state = client.getState();
            return state.isConnected();
        }
        return false;
    }

    private @NotNull Mqtt5Client connectMqtt5Client(
            final @NotNull ConnectOptions connectOptions,
            final @Nullable SubscribeOptions subscribeOptions) throws Exception {
        final MqttClientBuilder clientBuilder = createBuilder(connectOptions);
        final Mqtt5Client client = clientBuilder.useMqttVersion5()
                .advancedConfig()
                .interceptors()
                .incomingQos1Interceptor(new Mqtt5DebugIncomingQos1Interceptor())
                .outgoingQos1Interceptor(new Mqtt5DebugOutgoingQos1Interceptor())
                .incomingQos2Interceptor(new Mqtt5DebugIncomingQos2Interceptor())
                .outgoingQos2Interceptor(new Mqtt5DebugOutgoingQos2Interceptor())
                .applyInterceptors()
                .applyAdvancedConfig()
                .build();
        final Mqtt5Publish willPublish = createMqtt5WillPublish(connectOptions.getWillOptions());
        final Mqtt5ConnectRestrictions connectRestrictions =
                createMqtt5ConnectRestrictions(connectOptions.getConnectRestrictionOptions());

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder().willPublish(willPublish);

        // Workaround : if the built connect restrictions are the default ones do not append them to the connect builder
        // -> Else the connectMessage.toString() method will flood the logging output
        if (!connectRestrictions.equals(Mqtt5ConnectRestrictions.builder().build())) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.restrictions(connectRestrictions);
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

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING,
                        buildRemainingMqtt5PublishesCallback(subscribeOptions, client));


        System.setProperty("javax.net.debug", "ssl:handshake");

        mqtt5Connect(client, connectBuilder.build());

        final ClientData clientData = new ClientData(client);

        clientKeyToClientData.put(ClientKey.of(client), clientData);

        return client;
    }

    private @NotNull Mqtt3Client connectMqtt3Client(
            final @NotNull ConnectOptions connectOptions, final @Nullable SubscribeOptions subscribeOptions)
            throws Exception {
        final MqttClientBuilder clientBuilder = createBuilder(connectOptions);
        final Mqtt3Client client = clientBuilder.useMqttVersion3().build();

        final Mqtt3Publish willPublish = createMqtt3WillPublish(connectOptions.getWillOptions());

        final Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder().willPublish(willPublish);

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

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING,
                        buildRemainingMqtt3PublishesCallback(subscribeOptions, client));

        mqtt3Connect(client, connectBuilder.build());

        final ClientData clientData = new ClientData(client);

        clientKeyToClientData.put(ClientKey.of(client), clientData);

        return client;
    }

    private @Nullable Mqtt5Publish createMqtt5WillPublish(final @NotNull WillOptions willOptions) {
        // only topic is mandatory for will message creation
        if (willOptions.getWillTopic() != null) {
            final ByteBuffer willPayload = willOptions.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(willOptions.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(willOptions.getWillQos()))
                    .payloadFormatIndicator(willOptions.getWillPayloadFormatIndicator())
                    .contentType(willOptions.getWillContentType())
                    .responseTopic(willOptions.getWillResponseTopic())
                    .correlationData(willOptions.getWillCorrelationData());

            if (willOptions.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(willOptions.getWillRetain());
            }
            if (willOptions.getWillMessageExpiryInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.messageExpiryInterval(willOptions.getWillMessageExpiryInterval());
            }
            if (willOptions.getWillDelayInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.delayInterval(willOptions.getWillDelayInterval());
            }
            if (willOptions.getWillUserProperties() != null) { // user Properties can't be completed with null
                //noinspection ResultOfMethodCallIgnored
                builder.userProperties(willOptions.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (willOptions.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - will options were: {}",
                    willOptions.toString());
        }
        return null;
    }

    private @Nullable Mqtt3Publish createMqtt3WillPublish(final @NotNull WillOptions willOptions) {
        if (willOptions.getWillTopic() != null) {
            final ByteBuffer willPayload = willOptions.getWillMessage();
            final Mqtt3PublishBuilder.Complete builder = Mqtt3Publish.builder()
                    .topic(willOptions.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(willOptions.getWillQos()));

            if (willOptions.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(willOptions.getWillRetain());
            }
            return builder.build();
        } else if (willOptions.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - will options were: {}",
                    willOptions.toString());
        }
        return null;
    }

    private @NotNull Mqtt5ConnectRestrictions createMqtt5ConnectRestrictions(final @NotNull ConnectRestrictionOptions connectRestrictionOptions) {
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
        return restrictionsBuilder.build();
    }

    private @NotNull MqttClientBuilder createBuilder(final @NotNull ConnectOptions connectOptions) throws Exception {
        return MqttClient.builder()
                .addDisconnectedListener(new ContextClientDisconnectListener())
                .webSocketConfig(connectOptions.getWebSocketConfig())
                .serverHost(connectOptions.getHost())
                .serverPort(connectOptions.getPort())
                .sslConfig(connectOptions.buildSslConfig())
                .identifier(connectOptions.getIdentifier());
    }

    private @Nullable Mqtt5SimpleAuth buildMqtt5Authentication(final @NotNull AuthenticationOptions authenticationOptions) {
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

    private @Nullable Mqtt3SimpleAuth buildMqtt3Authentication(final @NotNull AuthenticationOptions authenticationOptions) {
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

    public static @NotNull Map<ClientKey, ClientData> getClientDataMap() {
        return clientKeyToClientData;
    }

    public @Nullable MqttClient getMqttClient(final @NotNull ClientKey clientKey) {
        MqttClient client = null;

        if (clientKeyToClientData.containsKey(clientKey)) {
            client = clientKeyToClientData.get(clientKey).getClient();
        }

        return client;
    }

    private @NotNull Consumer<Mqtt5Publish> buildRemainingMqtt5PublishesCallback(
            final @Nullable SubscribeOptions subscribeOptions, final @NotNull Mqtt5Client client) {
        if (subscribeOptions != null) {
            return new SubscribeMqtt5PublishCallback(subscribeOptions, client);
        } else {
            return mqtt5Publish -> Logger.debug("received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt5Publish,
                    new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }

    private @NotNull Consumer<Mqtt3Publish> buildRemainingMqtt3PublishesCallback(
            final @Nullable SubscribeOptions subscribeOptions, final @NotNull Mqtt3Client client) {
        if (subscribeOptions != null) {
            return new SubscribeMqtt3PublishCallback(subscribeOptions, client);
        } else {
            return mqtt3Publish -> Logger.debug("received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt3Publish,
                    new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }

}
