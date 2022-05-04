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

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.commands.cli.SubscribeCommand;
import com.hivemq.cli.utils.IntersectionUtil;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.cli.utils.Tuple;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

abstract class AbstractMqttClientExecutor {

    private static final @NotNull Map<String, ClientData> clientKeyToClientData = new ConcurrentHashMap<>();

    abstract void mqtt5Connect(
            final @NotNull Mqtt5Client client,
            final @NotNull Mqtt5Connect connectMessage,
            final @NotNull Connect connect);

    abstract void mqtt3Connect(
            final @NotNull Mqtt3Client client,
            final @NotNull Mqtt3Connect connectMessage,
            final @NotNull Connect connect);

    abstract void mqtt5Subscribe(
            final @NotNull Mqtt5Client client,
            final @NotNull Subscribe subscribe,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(
            final @NotNull Mqtt3Client client,
            final @NotNull Subscribe subscribe,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt5Publish(
            final @NotNull Mqtt5Client client,
            final @NotNull Publish publish,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt3Publish(
            final @NotNull Mqtt3Client client,
            final @NotNull Publish publish,
            final @NotNull String topic,
            final @NotNull MqttQos qos);

    abstract void mqtt5Unsubscribe(final @NotNull Mqtt5Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt3Unsubscribe(final @NotNull Mqtt3Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt5Disconnect(final @NotNull Mqtt5Client client, final @NotNull Disconnect disconnect);

    abstract void mqtt3Disconnect(final @NotNull Mqtt3Client client, final @NotNull Disconnect disconnect);

    public @NotNull MqttClient subscribe(final @NotNull SubscribeCommand subscribeCommand) {
        final MqttClient client = connect(subscribeCommand);

        subscribe(client, subscribeCommand);

        return client;
    }

    public void subscribe(final @NotNull MqttClient client, final @NotNull Subscribe subscribe) {
        for (int i = 0; i < subscribe.getTopics().length; i++) {
            final String topic = subscribe.getTopics()[i];

            // This check only works as subscribes are implemented blocking.
            // Otherwise, we would need to check the topics before they are iterated as they are added to the client data after a successful subscribe.
            final Tuple<MqttTopicFilter, MqttSharedTopicFilter> duplicateTopics =
                    checkForSharedTopicDuplicate(clientKeyToClientData.get(subscribe.getKey()), topic);
            if (duplicateTopics != null) {
                Logger.warn(
                        "WARN: A subscribed shared topic and normal topic intersect ({} and {}). " +
                                "This can lead to duplicate message output as multiple message callbacks are registered.",
                        duplicateTopics.getKey(),
                        duplicateTopics.getValue());
            }

            final int qosI = i < subscribe.getQos().length ? i : subscribe.getQos().length - 1;
            final MqttQos qos = subscribe.getQos()[qosI];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Subscribe((Mqtt5Client) client, subscribe, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Subscribe((Mqtt3Client) client, subscribe, topic, qos);
                    break;
            }
        }
    }

    @VisibleForTesting
    @Nullable Tuple<MqttTopicFilter, MqttSharedTopicFilter> checkForSharedTopicDuplicate(
            final @NotNull ClientData clientData, final @NotNull String topic) {
        final Set<MqttTopicFilter> subscribedFilters = clientData.getSubscribedTopics();
        final MqttTopicFilter newFilter = MqttTopicFilter.of(topic);
        if (subscribedFilters.stream().anyMatch(MqttTopicFilter::isShared)) {
            if (newFilter.isShared()) {
                final Set<MqttTopicFilter> normalFilters =
                        subscribedFilters.stream().filter(t -> !t.isShared()).collect(Collectors.toSet());
                for (final MqttTopicFilter normalFilter : normalFilters) {
                    final MqttSharedTopicFilter sharedFilter = ((MqttSharedTopicFilter) newFilter);
                    if (IntersectionUtil.intersects(normalFilter, sharedFilter.getTopicFilter())) {
                        return Tuple.of(normalFilter, sharedFilter);
                    }
                }
            } else {
                final Set<MqttTopicFilter> sharedTopics =
                        subscribedFilters.stream().filter(MqttTopicFilter::isShared).collect(Collectors.toSet());
                for (final MqttTopicFilter sharedTopic : sharedTopics) {
                    final MqttSharedTopicFilter sharedFilter = ((MqttSharedTopicFilter) sharedTopic);
                    if (IntersectionUtil.intersects(newFilter, sharedFilter.getTopicFilter())) {
                        return Tuple.of(newFilter, sharedFilter);
                    }
                }
            }
        } else {
            if (newFilter.isShared()) {
                for (final MqttTopicFilter subscribedFilter : subscribedFilters) {
                    final MqttSharedTopicFilter sharedFilter = ((MqttSharedTopicFilter) newFilter);
                    if (IntersectionUtil.intersects(sharedFilter.getTopicFilter(), subscribedFilter)) {
                        return Tuple.of(subscribedFilter, sharedFilter);
                    }
                }
            }
        }
        return null;
    }

    public void publish(final @NotNull PublishCommand publishCommand) {
        final MqttClient client = connect(publishCommand);

        publish(client, publishCommand);
    }

    public void publish(final @NotNull MqttClient client, final @NotNull Publish publish) {
        for (int i = 0; i < publish.getTopics().length; i++) {
            final String topic = publish.getTopics()[i];
            final int qosI = i < publish.getQos().length ? i : publish.getQos().length - 1;
            final MqttQos qos = publish.getQos()[qosI];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Publish((Mqtt5Client) client, publish, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Publish((Mqtt3Client) client, publish, topic, qos);
                    break;
            }
        }
    }

    public void disconnect(final @NotNull Disconnect disconnect) {
        final String clientKey = disconnect.getKey();

        if (clientKeyToClientData.containsKey(clientKey)) {
            final MqttClient client = clientKeyToClientData.get(clientKey).getClient();

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Disconnect((Mqtt5Client) client, disconnect);
                    break;
                case MQTT_3_1_1:
                    mqtt3Disconnect((Mqtt3Client) client, disconnect);
                    break;
            }
            clientKeyToClientData.remove(clientKey);
        } else {
            Logger.error("client to disconnect is not connected ({}) ", clientKey);
        }
    }

    public void disconnectAllClients(final @NotNull Disconnect disconnect) {
        for (final Map.Entry<String, ClientData> entry : clientKeyToClientData.entrySet()) {
            final MqttClient client = entry.getValue().getClient();
            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Disconnect((Mqtt5Client) client, disconnect);
                    break;
                case MQTT_3_1_1:
                    mqtt3Disconnect((Mqtt3Client) client, disconnect);
                    break;
            }
        }
        clientKeyToClientData.clear();
    }

    public void unsubscribe(final @NotNull MqttClient client, final @NotNull Unsubscribe unsubscribe) {
        switch (client.getConfig().getMqttVersion()) {
            case MQTT_5_0:
                mqtt5Unsubscribe((Mqtt5Client) client, unsubscribe);
                break;
            case MQTT_3_1_1:
                mqtt3Unsubscribe((Mqtt3Client) client, unsubscribe);
                break;
        }

    }

    public boolean isConnected(final @NotNull Context context) {
        if (clientKeyToClientData.containsKey(context.getKey())) {
            final MqttClient client = clientKeyToClientData.get(context.getKey()).getClient();
            final MqttClientState state = client.getState();
            return state.isConnected();
        }
        return false;
    }

    public @NotNull MqttClient connect(final @NotNull Connect connect) {
        if (isConnected(connect)) {
            Logger.debug("Client is already connected ({})", connect.getKey());
            Logger.info("Using already connected  ({})", connect.getKey());
            return clientKeyToClientData.get(connect.getKey()).getClient();
        }

        switch (connect.getVersion()) {
            case MQTT_5_0:
                return connectMqtt5Client(connect);
            case MQTT_3_1_1:
                return connectMqtt3Client(connect);
        }

        throw new IllegalStateException(
                "The MQTT Version specified is not supported. Version was " + connect.getVersion());
    }

    private @NotNull Mqtt5Client connectMqtt5Client(final @NotNull Connect connect) {
        final MqttClientBuilder clientBuilder = createBuilder(connect);
        final Mqtt5Client client = clientBuilder.useMqttVersion5().build();
        final Mqtt5Publish willPublish = createMqtt5WillPublish(connect);
        final Mqtt5ConnectRestrictions connectRestrictions = createMqtt5ConnectRestrictions(connect);

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder().willPublish(willPublish);

        // Workaround : if the built connect restrictions are the default ones do not append them to the connect builder
        // -> Else the connectMessage.toString() method will flood the logging output
        if (!connectRestrictions.equals(Mqtt5ConnectRestrictions.builder().build())) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.restrictions(connectRestrictions);
        }

        if (connect.getCleanStart() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.cleanStart(connect.getCleanStart());
        }
        if (connect.getKeepAlive() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.keepAlive(connect.getKeepAlive());
        }
        if (connect.getSessionExpiryInterval() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.sessionExpiryInterval(connect.getSessionExpiryInterval());
        }
        if (connect.getConnectUserProperties() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.userProperties(connect.getConnectUserProperties());
        }

        //noinspection ResultOfMethodCallIgnored
        connectBuilder.simpleAuth(buildMqtt5Authentication(connect));

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING, buildRemainingMqtt5PublishesCallback(connect, client));

        mqtt5Connect(client, connectBuilder.build(), connect);

        final ClientData clientData = new ClientData(client);

        final String key = MqttUtils.buildKey(
                client.getConfig().getClientIdentifier().map(Object::toString).orElse(""),
                client.getConfig().getServerHost());

        clientKeyToClientData.put(key, clientData);

        return client;
    }

    private @NotNull Mqtt3Client connectMqtt3Client(final @NotNull Connect connect) {
        final MqttClientBuilder clientBuilder = createBuilder(connect);
        final Mqtt3Client client = clientBuilder.useMqttVersion3().build();

        final Mqtt3Publish willPublish = createMqtt3WillPublish(connect);

        final Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder().willPublish(willPublish);

        if (connect.getCleanStart() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.cleanSession(connect.getCleanStart());
        }
        if (connect.getKeepAlive() != null) {
            //noinspection ResultOfMethodCallIgnored
            connectBuilder.keepAlive(connect.getKeepAlive());
        }

        //noinspection ResultOfMethodCallIgnored
        connectBuilder.simpleAuth(buildMqtt3Authentication(connect));

        client.toAsync()
                .publishes(MqttGlobalPublishFilter.REMAINING, buildRemainingMqtt3PublishesCallback(connect, client));

        mqtt3Connect(client, connectBuilder.build(), connect);

        final ClientData clientData = new ClientData(client);

        final String key = MqttUtils.buildKey(
                client.getConfig().getClientIdentifier().map(Object::toString).orElse(""),
                client.getConfig().getServerHost());

        clientKeyToClientData.put(key, clientData);

        return client;
    }

    private @Nullable Mqtt5Publish createMqtt5WillPublish(final @NotNull Will will) {
        // only topic is mandatory for will message creation
        if (will.getWillTopic() != null) {
            final ByteBuffer willPayload = will.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(will.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(will.getWillQos()))
                    .payloadFormatIndicator(will.getWillPayloadFormatIndicator())
                    .contentType(will.getWillContentType())
                    .responseTopic(will.getWillResponseTopic())
                    .correlationData(will.getWillCorrelationData());

            if (will.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(will.getWillRetain());
            }
            if (will.getWillMessageExpiryInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.messageExpiryInterval(will.getWillMessageExpiryInterval());
            }
            if (will.getWillDelayInterval() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.delayInterval(will.getWillDelayInterval());
            }
            if (will.getWillUserProperties() != null) { // user Properties can't be completed with null
                //noinspection ResultOfMethodCallIgnored
                builder.userProperties(will.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (will.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", will.toString());
        }
        return null;
    }

    private @Nullable Mqtt3Publish createMqtt3WillPublish(final @NotNull Will will) {
        if (will.getWillTopic() != null) {
            final ByteBuffer willPayload = will.getWillMessage();
            final Mqtt3PublishBuilder.Complete builder = Mqtt3Publish.builder()
                    .topic(will.getWillTopic())
                    .payload(willPayload)
                    .qos(Objects.requireNonNull(will.getWillQos()));

            if (will.getWillRetain() != null) {
                //noinspection ResultOfMethodCallIgnored
                builder.retain(will.getWillRetain());
            }
            return builder.build();
        } else if (will.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", will.toString());
        }
        return null;
    }

    private @NotNull Mqtt5ConnectRestrictions createMqtt5ConnectRestrictions(final @NotNull ConnectRestrictions connectRestrictions) {
        final Mqtt5ConnectRestrictionsBuilder restrictionsBuilder = Mqtt5ConnectRestrictions.builder();

        if (connectRestrictions.getReceiveMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.receiveMaximum(connectRestrictions.getReceiveMaximum());
        }
        if (connectRestrictions.getSendMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendMaximum(connectRestrictions.getSendMaximum());
        }
        if (connectRestrictions.getMaximumPacketSize() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.maximumPacketSize(connectRestrictions.getMaximumPacketSize());
        }
        if (connectRestrictions.getSendMaximumPacketSize() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendMaximumPacketSize(connectRestrictions.getSendMaximumPacketSize());
        }
        if (connectRestrictions.getTopicAliasMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.topicAliasMaximum(connectRestrictions.getTopicAliasMaximum());
        }
        if (connectRestrictions.getSendTopicAliasMaximum() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.sendTopicAliasMaximum(connectRestrictions.getSendTopicAliasMaximum());
        }
        if (connectRestrictions.getRequestProblemInformation() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.requestProblemInformation(connectRestrictions.getRequestProblemInformation());
        }
        if (connectRestrictions.getRequestResponseInformation() != null) {
            //noinspection ResultOfMethodCallIgnored
            restrictionsBuilder.requestResponseInformation(connectRestrictions.getRequestResponseInformation());
        }
        return restrictionsBuilder.build();
    }

    private @NotNull MqttClientBuilder createBuilder(final @NotNull Connect connect) {
        return MqttClient.builder()
                .addDisconnectedListener(new ContextClientDisconnectListener())
                .webSocketConfig(connect.getWebSocketConfig())
                .serverHost(connect.getHost())
                .serverPort(connect.getPort())
                .sslConfig(connect.getSslConfig())
                .identifier(connect.getIdentifier());
    }

    private @Nullable Mqtt5SimpleAuth buildMqtt5Authentication(final @NotNull Connect connect) {
        if (connect.getUser() != null && connect.getPassword() != null) {
            return Mqtt5SimpleAuth.builder().username(connect.getUser()).password(connect.getPassword()).build();
        } else if (connect.getPassword() != null) {
            return Mqtt5SimpleAuth.builder().password(connect.getPassword()).build();
        } else if (connect.getUser() != null) {
            return Mqtt5SimpleAuth.builder().username(connect.getUser()).build();
        }
        return null;
    }

    private @Nullable Mqtt3SimpleAuth buildMqtt3Authentication(final @NotNull Connect connect) {
        if (connect.getUser() != null && connect.getPassword() != null) {
            return Mqtt3SimpleAuth.builder().username(connect.getUser()).password(connect.getPassword()).build();
        } else if (connect.getUser() != null) {
            return Mqtt3SimpleAuth.builder().username(connect.getUser()).build();
        } else if (connect.getPassword() != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
        return null;
    }

    public static @NotNull Map<String, ClientData> getClientDataMap() {
        return clientKeyToClientData;
    }

    public @Nullable MqttClient getMqttClient(final @NotNull Context context) {
        MqttClient client = null;

        if (clientKeyToClientData.containsKey(context.getKey())) {
            client = clientKeyToClientData.get(context.getKey()).getClient();
        }

        return client;
    }

    private @NotNull Consumer<Mqtt5Publish> buildRemainingMqtt5PublishesCallback(
            final @NotNull Connect connect, final @NotNull Mqtt5Client client) {
        if (connect instanceof Subscribe) {
            return new SubscribeMqtt5PublishCallback((Subscribe) connect, client);
        } else {
            return mqtt5Publish -> Logger.debug(
                    "received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt5Publish,
                    new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }

    private @NotNull Consumer<Mqtt3Publish> buildRemainingMqtt3PublishesCallback(
            final @NotNull Connect connect, final @NotNull Mqtt3Client client) {
        if (connect instanceof Subscribe) {
            return new SubscribeMqtt3PublishCallback((Subscribe) connect, client);
        } else {
            return mqtt3Publish -> Logger.debug(
                    "received PUBLISH: {}, MESSAGE: '{}'",
                    mqtt3Publish,
                    new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
    }
}
