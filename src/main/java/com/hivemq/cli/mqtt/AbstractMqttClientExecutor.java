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
import com.hivemq.cli.commands.cli.SubscribeCommand;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
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
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

abstract class AbstractMqttClientExecutor {

    private static final Map<String, ClientData> clientKeyToClientData = new HashMap<>();


    abstract void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull Connect connect);

    abstract void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull Connect connect);

    abstract void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull Subscribe subscribe, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull Publish publish, final @NotNull String topic, final @NotNull MqttQos qos);

    abstract void mqtt5Unsubscribe(final @NotNull Mqtt5Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt3Unsubscribe(final @NotNull Mqtt3Client client, final @NotNull Unsubscribe unsubscribe);

    abstract void mqtt5Disconnect(final @NotNull Mqtt5Client client, final @NotNull Disconnect disconnect);

    abstract void mqtt3Disconnect(final @NotNull Mqtt3Client client, final @NotNull Disconnect disconnect);


    public void subscribe(final @NotNull SubscribeCommand subscribeCommand) {

        final MqttClient client = connect(subscribeCommand);

        subscribe(client, subscribeCommand);

    }

    public void subscribe(final @NotNull MqttClient client, final @NotNull Subscribe subscribe) {
        LoggingContext.put("identifier", "CLIENT " + client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < subscribe.getTopics().length; i++) {
            final String topic = subscribe.getTopics()[i];

            int qosI = i < subscribe.getQos().length ? i: subscribe.getQos().length-1;
            final MqttQos qos = subscribe.getQos()[qosI];

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

        final MqttClient client = connect(publishCommand);;

        publish(client, publishCommand);

    }

    public void publish(final @NotNull MqttClient client, final @NotNull Publish publish) {
        LoggingContext.put("identifier", "CLIENT " + client.getConfig().getClientIdentifier().get());

        for (int i = 0; i < publish.getTopics().length; i++) {
            final String topic = publish.getTopics()[i];
            int qosI = i < publish.getQos().length ? i: publish.getQos().length-1;
            final MqttQos qos = publish.getQos()[qosI];

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

        LoggingContext.put("identifier", "CLIENT " + disconnect.getIdentifier());


        if (clientKeyToClientData.containsKey(disconnect.getKey())) {
            final MqttClient client = clientKeyToClientData.get(disconnect.getKey()).getClient();

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

        LoggingContext.put("identifier", "CLIENT " + unsubscribe.getIdentifier());

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

        LoggingContext.put("identifier", "CLIENT " + context.getIdentifier());

        if (clientKeyToClientData.containsKey(context.getKey())) {
            final MqttClient client = clientKeyToClientData.get(context.getKey()).getClient();
            final MqttClientState state = client.getState();
            if (context.isVerbose()) {
                Logger.trace("in State: {}", state);
            }
            return state.isConnected();
        }
        return false;
    }


    public @NotNull MqttClient connect(final @NotNull Connect connect) {

        LoggingContext.put("identifier", "CLIENT " + connect.getIdentifier());

        if (isConnected(connect)) {
            final MqttClient client = clientKeyToClientData.get(connect.getKey()).getClient();
            switch (connect.getVersion()) {
                case MQTT_5_0:
                    ((Mqtt5Client) client).toBlocking().disconnect();
                    break;
                case MQTT_3_1_1:
                    ((Mqtt3Client) client).toBlocking().disconnect();
            }
            if (connect.isVerbose()) {
                Logger.trace("DISCONNECTED");
            }
            else if (connect.isDebug()) {
                Logger.debug("DISCONNECTED");
            }
        }

        switch (connect.getVersion()) {
            case MQTT_5_0:
                return connectMqtt5Client(connect);
            case MQTT_3_1_1:
                return connectMqtt3Client(connect);
        }

        throw new IllegalStateException("The MQTT Version specified is not supported. Version was " + connect.getVersion());
    }

    private @NotNull Mqtt5AsyncClient connectMqtt5Client(final @NotNull Connect connect) {

        final MqttClientBuilder clientBuilder = createBuilder(connect);
        final Mqtt5BlockingClient client = clientBuilder.useMqttVersion5().build().toBlocking();
        final @Nullable Mqtt5Publish willPublish = createMqtt5WillPublish(connect);
        final @NotNull Mqtt5ConnectRestrictions connectRestrictions = createMqtt5ConnectRestrictions(connect);

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
                .willPublish(willPublish)
                .restrictions(connectRestrictions);

        if (connect.getCleanStart() != null) {
            connectBuilder.cleanStart(connect.getCleanStart());
        }

        if (connect.getKeepAlive() != null) {
            connectBuilder.keepAlive(connect.getKeepAlive());
        }

        if (connect.getSessionExpiryInterval() != null) {
            connectBuilder.sessionExpiryInterval(connect.getSessionExpiryInterval());
        }

        if (connect.getConnectUserProperties() != null) {
            connectBuilder.userProperties(connect.getConnectUserProperties());
        }

        connectBuilder.simpleAuth(buildMqtt5Authentication(connect));

        mqtt5Connect(client, connectBuilder.build(), connect);

        final ClientData clientData = new ClientData(client);

        clientKeyToClientData.put(connect.getKey(), clientData);

        return client.toAsync();
    }

    private @NotNull Mqtt3AsyncClient connectMqtt3Client(final @NotNull Connect connect) {
        final MqttClientBuilder clientBuilder = createBuilder(connect);
        final Mqtt3BlockingClient client = clientBuilder.useMqttVersion3().build().toBlocking();

        final @Nullable Mqtt3Publish willPublish = createMqtt3WillPublish(connect);

        Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder()
                    .willPublish(willPublish);

        if (connect.getCleanStart() != null) {
            connectBuilder.cleanSession(connect.getCleanStart());
        }

        if (connect.getKeepAlive() != null) {
            connectBuilder.keepAlive(connect.getKeepAlive());
        }

        connectBuilder.simpleAuth(buildMqtt3Authentication(connect));

        mqtt3Connect(client, connectBuilder.build(), connect);

        final ClientData clientData = new ClientData(client);

        clientKeyToClientData.put(connect.getKey(), clientData);

        return client.toAsync();
    }

    private @Nullable Mqtt5Publish createMqtt5WillPublish(final @NotNull Will will) {
        // only topic is mandatory for will message creation
        if (will.getWillTopic() != null) {
            final ByteBuffer willPayload = will.getWillMessage();
            final Mqtt5WillPublishBuilder.Complete builder = Mqtt5WillPublish.builder()
                    .topic(will.getWillTopic())
                    .payload(willPayload)
                    .qos(will.getWillQos())
                    .payloadFormatIndicator(will.getWillPayloadFormatIndicator())
                    .contentType(will.getWillContentType())
                    .responseTopic(will.getWillResponseTopic())
                    .correlationData(will.getWillCorrelationData());

            if (will.getWillRetain() != null) {
                builder.retain(will.getWillRetain());
            }
            if (will.getWillMessageExpiryInterval() != null) {
                builder.messageExpiryInterval(will.getWillMessageExpiryInterval());
            }
            if (will.getWillDelayInterval() != null) {
                builder.delayInterval(will.getWillDelayInterval());
            }
            if (will.getWillUserProperties() != null) { // user Properties can't be completed with null
                builder.userProperties(will.getWillUserProperties());
            }
            return builder.build().asWill();
        }
        else if (will.getWillMessage() != null) {
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
                    .qos(will.getWillQos());

            if (will.getWillRetain() != null) {
                builder.retain(will.getWillRetain());
            }

            return builder.build();

        }
        else if (will.getWillMessage() != null) {
            Logger.warn("option -wt is missing if a will message is configured - command was: {} ", will.toString());

        }
        return null;
    }

    private @NotNull Mqtt5ConnectRestrictions createMqtt5ConnectRestrictions(final @NotNull ConnectRestrictions connectRestrictions) {

        final Mqtt5ConnectRestrictionsBuilder restrictionsBuilder = Mqtt5ConnectRestrictions.builder();

        if (connectRestrictions.getReceiveMaximum() != null) {
            restrictionsBuilder.receiveMaximum(connectRestrictions.getReceiveMaximum());
        }

        if (connectRestrictions.getSendMaximum() != null) {
            restrictionsBuilder.sendMaximum(connectRestrictions.getSendMaximum());
        }

        if (connectRestrictions.getMaximumPacketSize() != null) {
            restrictionsBuilder.maximumPacketSize(connectRestrictions.getMaximumPacketSize());
        }

        if (connectRestrictions.getSendMaximumPacketSize() != null) {
            restrictionsBuilder.sendMaximumPacketSize(connectRestrictions.getSendMaximumPacketSize());
        }

        if (connectRestrictions.getTopicAliasMaximum() != null) {
            restrictionsBuilder.topicAliasMaximum(connectRestrictions.getTopicAliasMaximum());
        }

        if (connectRestrictions.getSendTopicAliasMaximum() != null) {
            restrictionsBuilder.sendTopicAliasMaximum(connectRestrictions.getSendTopicAliasMaximum());
        }

        if (connectRestrictions.getRequestProblemInformation() != null) {
            restrictionsBuilder.requestProblemInformation(connectRestrictions.getRequestProblemInformation());
        }

        if (connectRestrictions.getRequestResponseInformation() != null) {
            restrictionsBuilder.requestResponseInformation(connectRestrictions.getRequestResponseInformation());
        }

        return restrictionsBuilder.build();
    }

    private @NotNull MqttClientBuilder createBuilder(final @NotNull Connect connect) {

        return MqttClient.builder()
                .addDisconnectedListener(new ContextClientDisconnectListener())
                .serverHost(connect.getHost())
                .serverPort(connect.getPort())
                .sslConfig(connect.getSslConfig())
                .identifier(connect.getIdentifier());
    }

    private @Nullable Mqtt5SimpleAuth buildMqtt5Authentication(final @NotNull Connect connect) {
        if (connect.getUser() != null && connect.getPassword() != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(connect.getUser())
                    .password(connect.getPassword())
                    .build();
        }
        else if (connect.getPassword() != null) {
            return Mqtt5SimpleAuth.builder()
                    .password(connect.getPassword())
                    .build();
        }
        else if (connect.getUser() != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(connect.getUser())
                    .build();
        }
        return null;
    }

    private @Nullable Mqtt3SimpleAuth buildMqtt3Authentication(final @NotNull Connect connect) {
        if (connect.getUser() != null && connect.getPassword() != null) {
            return Mqtt3SimpleAuth.builder()
                    .username(connect.getUser())
                    .password(connect.getPassword())
                    .build();
        }
        else if (connect.getUser() != null) {
            Mqtt3SimpleAuth.builder()
                    .username(connect.getUser())
                    .build();
        }
        else if (connect.getPassword() != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
        return null;
    }

    public static Map<String, ClientData> getClientDataMap() {
        return clientKeyToClientData;
    }

    public @Nullable MqttClient getMqttClient(final @NotNull Context context) {

        MqttClient client = null;

        if (clientKeyToClientData.containsKey(context.getKey())) {
            client = clientKeyToClientData.get(context.getKey()).getClient();
        }

        return client;
    }
}
