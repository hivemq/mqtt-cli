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
import com.hivemq.cli.commands.cli.ConnectCommand;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.commands.cli.SubscribeCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMqttClientExecutor {

    private static final ClientCache<String, MqttClient> clientCache = new ClientCache<>();
    private static final Map<String, LocalDateTime> clientCreationTimes = new HashMap<>();


    abstract void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand);

    abstract void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Unsubscribe(final @NotNull Mqtt5Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt3Unsubscribe(final @NotNull Mqtt3Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt5Disconnect(final @NotNull Mqtt5Client client, final @NotNull Disconnect disconnect);

    abstract void mqtt3Disconnect(final @NotNull Mqtt3Client client, final @NotNull Disconnect disconnect);


    public void subscribe(final @NotNull SubscribeCommand subscribeCommand) {

        final MqttClient client = getMqttClientFromCacheOrConnect(subscribeCommand);

        subscribe(client, subscribeCommand);

    }

    public void subscribe(final @NotNull MqttClient client, final @NotNull Subscribe subscribe) {
        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < subscribe.getTopics().length; i++) {
            final String topic = subscribe.getTopics()[i];
            final MqttQos qos = subscribe.getQos()[i];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Subscribe((Mqtt5AsyncClient) client, subscribe, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Subscribe((Mqtt3AsyncClient) client, subscribe, topic, qos);
                    break;
            }
        }
    }

    public void publish(final @NotNull PublishCommand publishCommand) {

        final MqttClient client = getMqttClientFromCacheOrConnect(publishCommand);
        publish(client, publishCommand);

    }

    public void publish(final @NotNull MqttClient client, final @NotNull Publish publish) {
        LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < publish.getTopics().length; i++) {
            final String topic = publish.getTopics()[i];
            final MqttQos qos = publish.getQos()[i];

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Publish((Mqtt5AsyncClient) client, publish, topic, qos);
                    break;
                case MQTT_3_1_1:
                    mqtt3Publish((Mqtt3AsyncClient) client, publish, topic, qos);
                    break;
            }

        }
    }

    public void disconnect(final @NotNull Disconnect disconnect) {

        LoggingContext.put("identifier", disconnect.getIdentifier());

        clientCache.setVerbose(disconnect.isVerbose());

        if (clientCache.hasKey(disconnect.getKey())) {
            final MqttClient client = clientCache.get(disconnect.getKey());
            clientCache.remove(disconnect.getKey());

            switch (client.getConfig().getMqttVersion()) {
                case MQTT_5_0:
                    mqtt5Disconnect((Mqtt5Client) client, disconnect);
                    break;
                case MQTT_3_1_1:
                    mqtt3Disconnect((Mqtt3Client) client, disconnect);
                    break;
            }
        } else if (disconnect.isDebug()) {
            Logger.debug("client to disconnect is not connected: {} ", disconnect.getKey());
        }

    }

    public void unsubscribe(final @NotNull MqttClient client, final @NotNull Unsubscribe unsubscribe) {

        LoggingContext.put("identifier", unsubscribe.getIdentifier());

        switch (client.getConfig().getMqttVersion()) {
            case MQTT_5_0:
                mqtt5Unsubscribe((Mqtt5Client) client, unsubscribe);
                break;
            case MQTT_3_1_1:
                mqtt3Unsubscribe((Mqtt3Client) client, unsubscribe);
                break;
        }

    }


    public boolean isConnected(final @NotNull Subscribe subscriber) {

        LoggingContext.put("identifier", subscriber.getIdentifier());

        clientCache.setVerbose(subscriber.isVerbose());

        if (clientCache.hasKey(subscriber.getKey())) {
            final MqttClient client = clientCache.get(subscriber.getKey());
            final MqttClientState state = client.getState();
            if (subscriber.isVerbose()) {
                Logger.trace("in State: {}", state);
            }
            return state.isConnected();
        }
        return false;
    }


    public @NotNull MqttClient connect(final @NotNull ConnectCommand connectCommand) {

        final String identifier = connectCommand.createIdentifier();

        LoggingContext.put("identifier", identifier);

        clientCache.setVerbose(connectCommand.isVerbose());

        switch (connectCommand.getVersion()) {
            case MQTT_5_0:
                return connectMqtt5Client(connectCommand, identifier);
            case MQTT_3_1_1:
                return connectMqtt3Client(connectCommand, identifier);
        }

        throw new IllegalStateException("The MQTT Version specified is not supported. Version was " + connectCommand.getVersion());
    }

    private @NotNull Mqtt5AsyncClient connectMqtt5Client(final @NotNull ConnectCommand connectCommand, final String identifier) {
        final MqttClientBuilder clientBuilder = createBuilder(connectCommand, identifier);
        final Mqtt5BlockingClient client = clientBuilder.useMqttVersion5().build().toBlocking();
        final @Nullable Mqtt5Publish willPublish = createMqtt5WillPublish(connectCommand);

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
                .sessionExpiryInterval(connectCommand.getSessionExpiryInterval())
                .keepAlive(connectCommand.getKeepAlive())
                .cleanStart(connectCommand.isCleanStart())
                .willPublish(willPublish);

        if (connectCommand.getUserProperties() != null) {
            connectBuilder.userProperties(connectCommand.getUserProperties());
        }

        applyMqtt5Authentication(connectBuilder, connectCommand);

        mqtt5Connect(client, connectBuilder.build(), connectCommand);

        clientCache.put(connectCommand.getKey(), client.toAsync());
        clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        return client.toAsync();
    }

    private @NotNull Mqtt3AsyncClient connectMqtt3Client(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {
        final MqttClientBuilder clientBuilder = createBuilder(connectCommand, identifier);
        final Mqtt3BlockingClient client = clientBuilder.useMqttVersion3().build().toBlocking();

        final @Nullable Mqtt3Publish willPublish = createMqtt3WillPublish(connectCommand);

        Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder()
                    .keepAlive(connectCommand.getKeepAlive())
                    .cleanSession(connectCommand.isCleanStart())
                    .willPublish(willPublish);

        applyMqtt3Authentication(connectBuilder, connectCommand);

        mqtt3Connect(client, connectBuilder.build(), connectCommand);

        clientCache.put(connectCommand.getKey(), client.toAsync());
        clientCreationTimes.put(connectCommand.getKey(), LocalDateTime.now());

        return client.toAsync();
    }

    private @Nullable Mqtt5Publish createMqtt5WillPublish(final @NotNull ConnectCommand connectCommand) {
        // only topic is mandatory for will message creation
        if (connectCommand.getWillTopic() != null) {
            final ByteBuffer willPayload = connectCommand.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willPayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .delayInterval(connectCommand.getWillDelayInterval())
                    .payloadFormatIndicator(connectCommand.getWillPayloadFormatIndicator())
                    .contentType(connectCommand.getWillContentType())
                    .responseTopic(connectCommand.getWillResponseTopic())
                    .correlationData(connectCommand.getWillCorrelationData());
            if (connectCommand.getWillMessageExpiryInterval() != null) {
                builder.messageExpiryInterval(connectCommand.getWillMessageExpiryInterval());
            }
            if (connectCommand.getWillUserProperties() != null) { // user Properties can't be completed with null
                builder.userProperties(connectCommand.getWillUserProperties());
            }
            return builder.build().asWill();
        } else if (connectCommand.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());
        }
        return null;
    }

    private @Nullable Mqtt3Publish createMqtt3WillPublish(final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getWillTopic() != null) {
            final ByteBuffer willPayload = connectCommand.getWillMessage();
            return Mqtt3Publish.builder()
                    .topic(connectCommand.getWillTopic())
                    .payload(willPayload)
                    .qos(connectCommand.getWillQos())
                    .retain(connectCommand.isWillRetain())
                    .build();
        } else if (connectCommand.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", connectCommand.toString());

        }
        return null;
    }

    private @NotNull MqttClientBuilder createBuilder(final @NotNull ConnectCommand connectCommand, final @NotNull String identifier) {

        return MqttClient.builder()
                .serverHost(connectCommand.getHost())
                .serverPort(connectCommand.getPort())
                .sslConfig(connectCommand.getSslConfig())
                .identifier(identifier);
    }

    private void applyMqtt5Authentication(final @NotNull Mqtt5ConnectBuilder connectBuilder, final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getUser() != null && connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getUser() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .applySimpleAuth();
        }
    }

    private void applyMqtt3Authentication(final @NotNull Mqtt3ConnectBuilder connectBuilder, final @NotNull ConnectCommand connectCommand) {
        if (connectCommand.getUser() != null && connectCommand.getPassword() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .password(connectCommand.getPassword())
                    .applySimpleAuth();
        } else if (connectCommand.getUser() != null) {
            connectBuilder.simpleAuth()
                    .username(connectCommand.getUser())
                    .applySimpleAuth();
        } else if (connectCommand.getPassword() != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
    }

    public static ClientCache<String, MqttClient> getClientCache() {
        return clientCache;
    }

    public Map<String, LocalDateTime> getClientCreationTimes() {
        return clientCreationTimes;
    }

    private MqttClient getMqttClientFromCacheOrConnect(final @NotNull ConnectCommand connect) {
        clientCache.setVerbose(connect.isVerbose());

        MqttClient mqttClient = null;

        if (clientCache.hasKey(connect.getKey())) {
            mqttClient = clientCache.get(connect.getKey());
        }

        if (mqttClient == null || (!mqttClient.getConfig().getState().isConnectedOrReconnect())) {
            mqttClient = connect(connect);
        }
        return mqttClient;
    }

    public @Nullable MqttClient getMqttClientFromCache(final @NotNull Context context) {
        clientCache.setVerbose(context.isVerbose());

        MqttClient client = null;

        if (clientCache.hasKey(context.getKey())) {
            client = clientCache.get(context.getKey());
        }

        return client;
    }
}
