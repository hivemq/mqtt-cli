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

import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.ConnectRestrictionOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt5PublishCallback;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.connack.MqttConnAckRestrictions;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.MqttSubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrAndOut;
import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CliMqtt5ClientTest {

    private static final @NotNull String CLIENT_ID = "test-client";
    private static final @NotNull String HOSTNAME = "test-broker.com";

    private final @NotNull Mqtt5Client delegate = mock(Mqtt5Client.class);
    private final @NotNull Mqtt5BlockingClient blockingDelegate = mock(Mqtt5BlockingClient.class);
    private final @NotNull Mqtt5AsyncClient asyncDelegate = mock(Mqtt5AsyncClient.class);
    private final @NotNull Mqtt5ClientConfig config = mock(Mqtt5ClientConfig.class);
    private final @NotNull CliMqtt5Client client = new CliMqtt5Client(delegate);

    private String clientLogPreamble;


    @BeforeEach
    void setUp() {
        when(delegate.getConfig()).thenReturn(config);
        when(delegate.toBlocking()).thenReturn(blockingDelegate);
        when(delegate.toAsync()).thenReturn(asyncDelegate);
        when(config.getClientIdentifier()).thenReturn(Optional.of(MqttClientIdentifier.of(CLIENT_ID)));
        when(config.getServerHost()).thenReturn(HOSTNAME);
        when(config.getMqttVersion()).thenReturn(MqttVersion.MQTT_3_1_1);
        when(config.getServerPort()).thenReturn(1883);
        this.clientLogPreamble = LoggerUtils.getClientPrefix(config);
    }

    @Test
    void connect_success() throws Exception {
        final Mqtt5ConnAck connack = new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS,
                false,
                100L,
                100,
                MqttClientIdentifierImpl.of(CLIENT_ID),
                null,
                MqttConnAckRestrictions.DEFAULT,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        final ConnectRestrictionOptions connectRestrictionOptions = new ConnectRestrictionOptions();
        when(blockingDelegate.connect(any(Mqtt5Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(connectRestrictionOptions);
        when(connectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt5Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanStart=false, sessionExpiryInterval=0, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=false, requestResponseInformation=false}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, sessionExpiryInterval=100, serverKeepAlive=100, assignedClientIdentifier=test-client}");
    }

    @Test
    void connect_whenUsernameAndPasswordArePresent_thenConnectIsSentWithUsernameAndPassword() throws Exception {
        final Mqtt5ConnAck connack = new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS,
                false,
                100L,
                100,
                MqttClientIdentifierImpl.of(CLIENT_ID),
                null,
                MqttConnAckRestrictions.DEFAULT,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        final ConnectRestrictionOptions connectRestrictionOptions = new ConnectRestrictionOptions();
        when(blockingDelegate.connect(any(Mqtt5Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(authenticationOptions.getUser()).thenReturn("user");
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(connectRestrictionOptions);
        when(connectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt5Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanStart=false, sessionExpiryInterval=0, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=false, requestResponseInformation=false}, simpleAuth=MqttSimpleAuth{username and password}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, sessionExpiryInterval=100, serverKeepAlive=100, assignedClientIdentifier=test-client}");
    }

    @Test
    void connect_whenUsernameIsPresent_thenConnectIsSentWithUsername() throws Exception {
        final Mqtt5ConnAck connack = new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS,
                false,
                100L,
                100,
                MqttClientIdentifierImpl.of(CLIENT_ID),
                null,
                MqttConnAckRestrictions.DEFAULT,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        final ConnectRestrictionOptions connectRestrictionOptions = new ConnectRestrictionOptions();
        when(blockingDelegate.connect(any(Mqtt5Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(connectRestrictionOptions);
        when(authenticationOptions.getUser()).thenReturn("user");
        when(connectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt5Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanStart=false, sessionExpiryInterval=0, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=false, requestResponseInformation=false}, simpleAuth=MqttSimpleAuth{username}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, sessionExpiryInterval=100, serverKeepAlive=100, assignedClientIdentifier=test-client}");
    }

    @Test
    void connect_whenPasswordIsPresent_thenConnectIsSentWithPassword() throws Exception {
        final Mqtt5ConnAck connack = new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS,
                false,
                100L,
                100,
                MqttClientIdentifierImpl.of(CLIENT_ID),
                null,
                MqttConnAckRestrictions.DEFAULT,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        final ConnectRestrictionOptions connectRestrictionOptions = new ConnectRestrictionOptions();
        when(blockingDelegate.connect(any(Mqtt5Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(connectRestrictionOptions);
        when(connectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt5Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanStart=false, sessionExpiryInterval=0, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=false, requestResponseInformation=false}, simpleAuth=MqttSimpleAuth{password}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{reasonCode=SUCCESS, sessionPresent=false, sessionExpiryInterval=100, serverKeepAlive=100, assignedClientIdentifier=test-client}");
    }

    @Test
    void publish_whenOneTopic_thenOnePublishIsSent() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        final MqttPublishResult publishResult =
                new MqttPublishResult(createPublish("test", MqttQos.EXACTLY_ONCE, "test"), null);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(publishOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.publish(any(Mqtt5Publish.class))).thenReturn(CompletableFuture.completedFuture(publishResult));

        final String output = tapSystemOut(() -> client.publish(publishOptions));

        verify(asyncDelegate).publish(any(Mqtt5Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic, payload=4byte, qos=EXACTLY_ONCE, retain=false, messageExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=test, payload=4byte, qos=EXACTLY_ONCE, retain=false, messageExpiryInterval=100}}");
    }

    @Test
    void publish_whenMultipleTopicsAndMultipleQos_thenMultiplePublishesAreSent() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(publishOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.publish(any(Mqtt5Publish.class))).thenReturn(CompletableFuture.completedFuture(new MqttPublishResult(
                        createPublish("topic1", MqttQos.AT_MOST_ONCE, "test"),
                        null)))
                .thenReturn(CompletableFuture.completedFuture(new MqttPublishResult(createPublish("topic2",
                        MqttQos.AT_LEAST_ONCE,
                        "test"), null)))
                .thenReturn(CompletableFuture.completedFuture(new MqttPublishResult(createPublish("topic3",
                        MqttQos.EXACTLY_ONCE,
                        "test"), null)));

        final String output = tapSystemOut(() -> client.publish(publishOptions));

        verify(asyncDelegate, times(3)).publish(any(Mqtt5Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic1, payload=4byte, qos=AT_MOST_ONCE, retain=false, messageExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic2, payload=4byte, qos=AT_LEAST_ONCE, retain=false, messageExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic3, payload=4byte, qos=EXACTLY_ONCE, retain=false, messageExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=topic1, payload=4byte, qos=AT_MOST_ONCE, retain=false, messageExpiryInterval=100}}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=topic2, payload=4byte, qos=AT_LEAST_ONCE, retain=false, messageExpiryInterval=100}}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublishResult{publish=MqttPublish{topic=topic3, payload=4byte, qos=EXACTLY_ONCE, retain=false, messageExpiryInterval=100}}");
    }

    @Test
    void publish_whenTopicsSizeDoesNotMatchQosSize_thenThrowsIllegalArgumentException() {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});

        assertThrows(IllegalArgumentException.class,
                () -> client.publish(publishOptions),
                "Topics size (2) does not match QoS size (1)");

        verify(asyncDelegate, times(0)).publish(any(Mqtt5Publish.class));
    }

    @Test
    void publish_whenPublishFails_thenThrowsCompletionException() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(publishOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        final CompletableFuture<Mqtt5PublishResult> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.publish(any(Mqtt5Publish.class))).thenReturn(future);

        final String output =
                tapSystemErrAndOut(() -> assertThrows(CompletionException.class, () -> client.publish(publishOptions)));

        verify(asyncDelegate).publish(any(Mqtt5Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic, payload=4byte, qos=EXACTLY_ONCE, retain=false, messageExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " failed PUBLISH to topic 'topic': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void subscribe_whenOneTopic_thenOneSubscribeIsSent() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final MqttSubAck suback = new MqttSubAck(1,
                ImmutableList.of(Mqtt5SubAckReasonCode.GRANTED_QOS_2),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final String output = tapSystemOut(() -> client.subscribe(subscribeOptions));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic, qos=EXACTLY_ONCE, noLocal=false, retainHandling=SEND, retainAsPublished=false}]}");
        assertThat(output).contains(clientLogPreamble +
                " received SUBACK MqttSubAck{reasonCodes=[GRANTED_QOS_2], packetIdentifier=1}");
    }

    @Test
    void subscribe_whenMultipleTopicsAndQos_thenOneSubscribeIsSent() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final MqttSubAck suback = new MqttSubAck(1,
                ImmutableList.of(Mqtt5SubAckReasonCode.GRANTED_QOS_0,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_1,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_2),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final String output = tapSystemOut(() -> client.subscribe(subscribeOptions));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic1, qos=AT_MOST_ONCE, noLocal=false, retainHandling=SEND, retainAsPublished=false}, MqttSubscription{topicFilter=topic2, qos=AT_LEAST_ONCE, noLocal=false, retainHandling=SEND, retainAsPublished=false}, MqttSubscription{topicFilter=topic3, qos=EXACTLY_ONCE, noLocal=false, retainHandling=SEND, retainAsPublished=false}]}");
        assertThat(output).contains(clientLogPreamble +
                " received SUBACK MqttSubAck{reasonCodes=[GRANTED_QOS_0, GRANTED_QOS_1, GRANTED_QOS_2], packetIdentifier=1}");
    }

    @Test
    void subscribe_whenTopicsSizeDoesNotMatchQosSize_thenThrowsIllegalArgumentException() {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.AT_MOST_ONCE});

        assertThrows(IllegalArgumentException.class,
                () -> client.subscribe(subscribeOptions),
                "Topics size (2) does not match QoS size (1)");

        verify(asyncDelegate, times(0)).subscribe(any(Mqtt5Subscribe.class));
    }

    @Test
    void subscribe_whenSubscribeFails_thenThrowsCompletionsException() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        final CompletableFuture<Mqtt5SubAck> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                future);
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.subscribe(subscribeOptions),
                "failed"));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic, qos=EXACTLY_ONCE, noLocal=false, retainHandling=SEND, retainAsPublished=false}]}");
        assertThat(output).contains(clientLogPreamble +
                " failed SUBSCRIBE to topic(s) '[topic]': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void unsubscribe_whenOneTopic_thenOneUnsubscribeIsSent() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        final MqttUnsubAck unsuback = new MqttUnsubAck(1,
                ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(unsubscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.unsubscribe(any(Mqtt5Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(
                unsuback));

        final String output = tapSystemOut(() -> client.unsubscribe(unsubscribeOptions));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt5Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble + " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic]}");
        assertThat(output).contains(clientLogPreamble +
                " received UNSUBACK MqttUnsubAck{reasonCodes=[SUCCESS], packetIdentifier=1}");
    }

    @Test
    void unsubscribe_whenMultipleTopics_thenOneUnsubscribeIsSent() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        final MqttUnsubAck unsuback = new MqttUnsubAck(1,
                ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS,
                        Mqtt5UnsubAckReasonCode.SUCCESS,
                        Mqtt5UnsubAckReasonCode.SUCCESS),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(unsubscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.unsubscribe(any(Mqtt5Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(unsuback));

        final String output = tapSystemOut(() -> client.unsubscribe(unsubscribeOptions));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt5Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble +
                " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic1, topic2, topic3]}");
        assertThat(output).contains(clientLogPreamble + " received UNSUBACK MqttUnsubAck{reasonCodes=[SUCCESS, SUCCESS, SUCCESS], packetIdentifier=1}");
    }

    @Test
    void unsubscribe_whenUnsubscribeFails_thenThrowsCompletionException() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        final CompletableFuture<Mqtt5UnsubAck> future = new CompletableFuture<>();
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(unsubscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.unsubscribe(any(Mqtt5Unsubscribe.class))).thenReturn(future);

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.unsubscribe(unsubscribeOptions),
                "failed"));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt5Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble + " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic]}");
        assertThat(output).contains(clientLogPreamble +
                " failed UNSUBSCRIBE from topic(s) '[topic]': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void disconnect_success() throws Exception {
        final DisconnectOptions disconnectOptions = mock(DisconnectOptions.class);
        when(asyncDelegate.disconnect(any(Mqtt5Disconnect.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(disconnectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final String output = tapSystemOut(() -> client.disconnect(disconnectOptions));

        verify(asyncDelegate, times(1)).disconnect(any(Mqtt5Disconnect.class));
        assertThat(output).contains(clientLogPreamble + " sending DISCONNECT MqttDisconnect{reasonCode=NORMAL_DISCONNECTION, sessionExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble + " disconnected successfully");
    }

    @Test
    void disconnect_whenDisconnectFails_thenThrowsCompletionException() throws Exception {
        final DisconnectOptions disconnectOptions = mock(DisconnectOptions.class);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(disconnectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.disconnect(any(Mqtt5Disconnect.class))).thenReturn(future);

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.disconnect(disconnectOptions),
                "failed"));

        verify(asyncDelegate, times(1)).disconnect(any(Mqtt5Disconnect.class));
        assertThat(output).contains(clientLogPreamble + " sending DISCONNECT MqttDisconnect{reasonCode=NORMAL_DISCONNECTION, sessionExpiryInterval=0}");
        assertThat(output).contains(clientLogPreamble +
                " failed to DISCONNECT gracefully: failed: java.lang.NullPointerException: failed");
    }

    @Test
    void isConnected_success() {
        when(delegate.getState()).thenReturn(MqttClientState.CONNECTED);
        assertTrue(client.isConnected());
    }

    @Test
    void getClientIdentifier_success() {
        assertEquals(CLIENT_ID, client.getClientIdentifier());
    }

    @Test
    void getServerHost_success() {
        assertEquals(HOSTNAME, client.getServerHost());
    }

    @Test
    void getMqttVersion_success() {
        assertEquals(MqttVersion.MQTT_3_1_1, client.getMqttVersion());
    }

    @Test
    void getConnectedAt_success() {
        final Mqtt5ConnAck connack = new MqttConnAck(Mqtt5ConnAckReasonCode.SUCCESS,
                false,
                100L,
                100,
                MqttClientIdentifierImpl.of(CLIENT_ID),
                null,
                MqttConnAckRestrictions.DEFAULT,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(blockingDelegate.connect(any(Mqtt5Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(connectOptions.getConnectRestrictionOptions()).thenReturn(new ConnectRestrictionOptions());
        when(connectOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        client.connect(connectOptions);
        assertNotNull(client.getConnectedAt());
    }

    @Test
    void getConnectedAt_whenNotConnected_thenThrowsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                client::getConnectedAt,
                "connectedAt must not be null after a client has connected successfully");
    }

    @Test
    void getState_success() {
        when(delegate.getState()).thenReturn(MqttClientState.DISCONNECTED);
        assertEquals(MqttClientState.DISCONNECTED, client.getState());
    }

    @Test
    void getSslProtocols_whenProtocolsArePresent_thenListStringIsReturned() {
        final MqttClientSslConfig sslConfig = mock(MqttClientSslConfig.class);
        when(sslConfig.getProtocols()).thenReturn(Optional.of(ImmutableList.of("TLS_1_2", "TLS_1_3")));
        when(config.getSslConfig()).thenReturn(Optional.of(sslConfig));

        assertEquals("[TLS_1_2, TLS_1_3]", client.getSslProtocols());
    }

    @Test
    void getSslProtocols_whenNoSslConfigIsPresent_thenNoSslStringIsReturned() {
        assertEquals("NO_SSL", client.getSslProtocols());
    }

    @Test
    void getServerPort_success() {
        assertEquals(1883, client.getServerPort());
    }

    @Test
    void getSubscribedTopics_whenSuccessfullySubscribed_thenSubscriptionsArePresent() {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final MqttSubAck suback = new MqttSubAck(1,
                ImmutableList.of(Mqtt5SubAckReasonCode.GRANTED_QOS_0,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_1,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_2),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        client.subscribe(subscribeOptions);
        final List<MqttTopicFilter> subscribedTopics = client.getSubscribedTopics();
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic1"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic2"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic3"));
    }

    @Test
    void getSubscribedTopics_whenSuccessfullySubscribedAndUnsubscribed_thenNoSubscriptionIsPresent() {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final MqttSubAck suback = new MqttSubAck(1,
                ImmutableList.of(Mqtt5SubAckReasonCode.GRANTED_QOS_0,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_1,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_2),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(unsubscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.unsubscribe(any(Mqtt5Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));

        client.subscribe(subscribeOptions);
        final List<MqttTopicFilter> subscribedTopics = client.getSubscribedTopics();
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic1"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic2"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic3"));

        client.unsubscribe(unsubscribeOptions);
        assertThat(client.getSubscribedTopics()).isEmpty();
    }

    @Test
    void getSubscribedTopics_whenSuccessfullySubscribedAndPartiallyUnsubscribed_thenNotUnsubscribedSubscriptionsArePresent() {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final MqttSubAck suback = new MqttSubAck(1,
                ImmutableList.of(Mqtt5SubAckReasonCode.GRANTED_QOS_0,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_1,
                        Mqtt5SubAckReasonCode.GRANTED_QOS_2),
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(subscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.subscribe(any(Mqtt5Subscribe.class), any(SubscribeMqtt5PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic3"});
        when(unsubscribeOptions.getUserProperties()).thenReturn(Mqtt5UserProperties.of());
        when(asyncDelegate.unsubscribe(any(Mqtt5Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));

        client.subscribe(subscribeOptions);
        final List<MqttTopicFilter> subscribedTopics = client.getSubscribedTopics();
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic1"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic2"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic3"));

        client.unsubscribe(unsubscribeOptions);
        assertThat(client.getSubscribedTopics()).containsExactly(MqttTopicFilter.of("topic2"));
    }

    private static @NotNull MqttPublish createPublish(
            final @NotNull String topic, final @NotNull MqttQos qos, final @NotNull String message) {
        return new MqttPublish(MqttTopicImpl.of(topic),
                ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)),
                qos,
                false,
                100L,
                null,
                null,
                null,
                null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                null);
    }

}
