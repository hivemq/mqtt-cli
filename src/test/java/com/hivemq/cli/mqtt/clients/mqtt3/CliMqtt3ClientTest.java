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

import com.hivemq.cli.commands.options.AuthenticationOptions;
import com.hivemq.cli.commands.options.ConnectOptions;
import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.commands.options.PublishOptions;
import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.commands.options.UnsubscribeOptions;
import com.hivemq.cli.mqtt.clients.listeners.SubscribeMqtt3PublishCallback;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
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

class CliMqtt3ClientTest {

    private static final @NotNull String CLIENT_ID = "test-client";
    private static final @NotNull String HOSTNAME = "test-broker.com";

    private final @NotNull Mqtt3Client delegate = mock(Mqtt3Client.class);
    private final @NotNull Mqtt3BlockingClient blockingDelegate = mock(Mqtt3BlockingClient.class);
    private final @NotNull Mqtt3AsyncClient asyncDelegate = mock(Mqtt3AsyncClient.class);
    private final @NotNull Mqtt3ClientConfig config = mock(Mqtt3ClientConfig.class);
    private final @NotNull CliMqtt3Client client = new CliMqtt3Client(delegate);

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
        final Mqtt3ConnAck connack =
                Mqtt3ConnAckView.of(Mqtt3ConnAckView.delegate(Mqtt3ConnAckReturnCode.SUCCESS, false));
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(blockingDelegate.connect(any(Mqtt3Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt3Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanSession=false, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=true, requestResponseInformation=false}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{returnCode=SUCCESS, sessionPresent=false} ");
    }

    @Test
    void connect_whenUsernameAndPasswordArePresent_thenConnectIsSentWithUsernameAndPassword() throws Exception {
        final Mqtt3ConnAck connack =
                Mqtt3ConnAckView.of(Mqtt3ConnAckView.delegate(Mqtt3ConnAckReturnCode.SUCCESS, false));
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(blockingDelegate.connect(any(Mqtt3Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(authenticationOptions.getUser()).thenReturn("user");
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt3Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanSession=false, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=true, requestResponseInformation=false}, simpleAuth=MqttSimpleAuth{username and password}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{returnCode=SUCCESS, sessionPresent=false}");
    }

    @Test
    void connect_whenUsernameIsPresent_thenConnectIsSentWithUsername() throws Exception {
        final Mqtt3ConnAck connack =
                Mqtt3ConnAckView.of(Mqtt3ConnAckView.delegate(Mqtt3ConnAckReturnCode.SUCCESS, false));
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(blockingDelegate.connect(any(Mqtt3Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(authenticationOptions.getUser()).thenReturn("user");

        final String output = tapSystemOut(() -> client.connect(connectOptions));

        verify(blockingDelegate).connect(any(Mqtt3Connect.class));
        assertThat(output).contains(clientLogPreamble +
                " sending CONNECT MqttConnect{keepAlive=0, cleanSession=false, restrictions=MqttConnectRestrictions{receiveMaximum=65535, sendMaximum=65535, maximumPacketSize=268435460, sendMaximumPacketSize=268435460, topicAliasMaximum=0, sendTopicAliasMaximum=16, requestProblemInformation=true, requestResponseInformation=false}, simpleAuth=MqttSimpleAuth{username}}");
        assertThat(output).contains(clientLogPreamble +
                " received CONNACK MqttConnAck{returnCode=SUCCESS, sessionPresent=false}");
    }

    @Test
    void connect_whenOnlyPasswordIsPresent_thenThrowsIllegalArgumentException() {
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);
        when(authenticationOptions.getPassword()).thenReturn(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));

        assertThrows(IllegalArgumentException.class,
                () -> client.connect(connectOptions),
                "Password-Only Authentication is not allowed in MQTT 3");

        verify(blockingDelegate, times(0)).connect(any(Mqtt3Connect.class));
    }

    @Test
    void publish_whenOneTopic_thenOnePublishIsSent() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.publish(any(Mqtt3Publish.class))).thenReturn(CompletableFuture.completedFuture(Mqtt3Publish.builder()
                .topic("topic")
                .qos(MqttQos.EXACTLY_ONCE)
                .build()));

        final String output = tapSystemOut(() -> client.publish(publishOptions));

        verify(asyncDelegate).publish(any(Mqtt3Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic, payload=4byte, qos=EXACTLY_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublish{topic=topic, qos=EXACTLY_ONCE, retain=false}");
    }

    @Test
    void publish_whenMultipleTopicsAndMultipleQos_thenMultiplePublishesAreSent() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.publish(any(Mqtt3Publish.class))).thenReturn(CompletableFuture.completedFuture(Mqtt3Publish.builder()
                        .topic("topic1")
                        .qos(MqttQos.AT_MOST_ONCE)
                        .build()))
                .thenReturn(CompletableFuture.completedFuture(Mqtt3Publish.builder()
                        .topic("topic2")
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .build()))
                .thenReturn(CompletableFuture.completedFuture(Mqtt3Publish.builder()
                        .topic("topic3")
                        .qos(MqttQos.EXACTLY_ONCE)
                        .build()));

        final String output = tapSystemOut(() -> client.publish(publishOptions));

        verify(asyncDelegate, times(3)).publish(any(Mqtt3Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic1, payload=4byte, qos=AT_MOST_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic2, payload=4byte, qos=AT_LEAST_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic3, payload=4byte, qos=EXACTLY_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublish{topic=topic1, qos=AT_MOST_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublish{topic=topic2, qos=AT_LEAST_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " received PUBLISH acknowledgement MqttPublish{topic=topic3, qos=EXACTLY_ONCE, retain=false}");
    }

    @Test
    void publish_whenTopicsSizeDoesNotMatchQosSize_thenThrowsIllegalArgumentException() {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.publish(any(Mqtt3Publish.class))).thenReturn(CompletableFuture.completedFuture(Mqtt3Publish.builder()
                .topic("topic")
                .qos(MqttQos.EXACTLY_ONCE)
                .build()));

        assertThrows(IllegalArgumentException.class,
                () -> client.publish(publishOptions),
                "Topics size (2) does not match QoS size (1)");
        verify(asyncDelegate, times(0)).publish(any(Mqtt3Publish.class));
    }

    @Test
    void publish_whenPublishFails_thenThrowsCompletionException() throws Exception {
        final PublishOptions publishOptions = mock(PublishOptions.class);
        when(publishOptions.getMessage()).thenReturn(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
        when(publishOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(publishOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        final CompletableFuture<Mqtt3Publish> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.publish(any(Mqtt3Publish.class))).thenReturn(future);

        final String output =
                tapSystemErrAndOut(() -> assertThrows(CompletionException.class, () -> client.publish(publishOptions)));

        verify(asyncDelegate).publish(any(Mqtt3Publish.class));
        assertThat(output).contains(clientLogPreamble +
                " sending PUBLISH ('test') MqttPublish{topic=topic, payload=4byte, qos=EXACTLY_ONCE, retain=false}");
        assertThat(output).contains(clientLogPreamble +
                " failed PUBLISH to topic 'topic': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void subscribe_whenOneTopic_thenOneSubscribeIsSent() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final Mqtt3SubAck suback = Mqtt3SubAckView.of(Mqtt3SubAckView.delegate(1,
                ImmutableList.of(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)));
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final String output = tapSystemOut(() -> client.subscribe(subscribeOptions));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic, qos=EXACTLY_ONCE}]}");
        assertThat(output).contains(clientLogPreamble +
                " received SUBACK MqttSubAck{returnCodes=[SUCCESS_MAXIMUM_QOS_2]}");
    }

    @Test
    void subscribe_whenMultipleTopicsAndQos_thenOneSubscribeIsSent() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        final Mqtt3SubAck suback = Mqtt3SubAckView.of(Mqtt3SubAckView.delegate(1,
                ImmutableList.of(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)));
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final String output = tapSystemOut(() -> client.subscribe(subscribeOptions));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic1, qos=AT_MOST_ONCE}, MqttSubscription{topicFilter=topic2, qos=AT_LEAST_ONCE}, MqttSubscription{topicFilter=topic3, qos=EXACTLY_ONCE}]}");
        assertThat(output).contains(clientLogPreamble +
                " received SUBACK MqttSubAck{returnCodes=[SUCCESS_MAXIMUM_QOS_0, SUCCESS_MAXIMUM_QOS_1, SUCCESS_MAXIMUM_QOS_2]}");
    }

    @Test
    void subscribe_whenTopicsSizeDoesNotMatchQosSize_thenThrowsIllegalArgumentException() {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.AT_MOST_ONCE});

        assertThrows(IllegalArgumentException.class,
                () -> client.subscribe(subscribeOptions),
                "Topics size (2) does not match QoS size (1)");
        verify(asyncDelegate, times(0)).subscribe(any(Mqtt3Subscribe.class));
    }

    @Test
    void subscribe_whenSubscribeFails_thenThrowsCompletionsException() throws Exception {
        final SubscribeOptions subscribeOptions = mock(SubscribeOptions.class);
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{MqttQos.EXACTLY_ONCE});
        final CompletableFuture<Mqtt3SubAck> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
                future);

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.subscribe(subscribeOptions),
                "failed"));

        verify(asyncDelegate, times(1)).subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class));
        assertThat(output).contains(clientLogPreamble +
                " sending SUBSCRIBE MqttSubscribe{subscriptions=[MqttSubscription{topicFilter=topic, qos=EXACTLY_ONCE}]}");
        assertThat(output).contains(clientLogPreamble +
                " failed SUBSCRIBE to topic(s) '[topic]': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void unsubscribe_whenOneTopic_thenOneUnsubscribeIsSent() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        when(asyncDelegate.unsubscribe(any(Mqtt3Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));

        final String output = tapSystemOut(() -> client.unsubscribe(unsubscribeOptions));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt3Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble + " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic]}");
        assertThat(output).contains(clientLogPreamble + " received UNSUBACK");
    }

    @Test
    void unsubscribe_whenMultipleTopics_thenOneUnsubscribeIsSent() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(asyncDelegate.unsubscribe(any(Mqtt3Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));

        final String output = tapSystemOut(() -> client.unsubscribe(unsubscribeOptions));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt3Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble +
                " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic1, topic2, topic3]}");
        assertThat(output).contains(clientLogPreamble + " received UNSUBACK");
    }

    @Test
    void unsubscribe_whenUnsubscribeFails_thenThrowsCompletionException() throws Exception {
        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic"});
        final CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.unsubscribe(any(Mqtt3Unsubscribe.class))).thenReturn(future);

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.unsubscribe(unsubscribeOptions),
                "failed"));

        verify(asyncDelegate, times(1)).unsubscribe(any(Mqtt3Unsubscribe.class));
        assertThat(output).contains(clientLogPreamble + " sending UNSUBSCRIBE MqttUnsubscribe{topicFilters=[topic]}");
        assertThat(output).contains(clientLogPreamble +
                " failed UNSUBSCRIBE from topic(s) '[topic]': failed: java.lang.NullPointerException: failed");
    }

    @Test
    void disconnect_success() throws Exception {
        final DisconnectOptions disconnectOptions = mock(DisconnectOptions.class);
        when(asyncDelegate.disconnect()).thenReturn(CompletableFuture.completedFuture(null));

        final String output = tapSystemOut(() -> client.disconnect(disconnectOptions));

        verify(asyncDelegate, times(1)).disconnect();
        assertThat(output).contains(clientLogPreamble + " sending DISCONNECT");
        assertThat(output).contains(clientLogPreamble + " disconnected successfully");
    }

    @Test
    void disconnect_whenDisconnectFails_thenThrowsCompletionException() throws Exception {
        final DisconnectOptions disconnectOptions = mock(DisconnectOptions.class);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new NullPointerException("failed"));
        when(asyncDelegate.disconnect()).thenReturn(future);

        final String output = tapSystemErrAndOut(() -> assertThrows(CompletionException.class,
                () -> client.disconnect(disconnectOptions),
                "failed"));

        verify(asyncDelegate, times(1)).disconnect();
        assertThat(output).contains(clientLogPreamble + " sending DISCONNECT");
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
        final Mqtt3ConnAck connack =
                Mqtt3ConnAckView.of(Mqtt3ConnAckView.delegate(Mqtt3ConnAckReturnCode.SUCCESS, false));
        final ConnectOptions connectOptions = mock(ConnectOptions.class);
        final AuthenticationOptions authenticationOptions = mock(AuthenticationOptions.class);
        when(blockingDelegate.connect(any(Mqtt3Connect.class))).thenReturn(connack);
        when(connectOptions.getAuthenticationOptions()).thenReturn(authenticationOptions);

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
        final Mqtt3SubAck suback = Mqtt3SubAckView.of(Mqtt3SubAckView.delegate(1,
                ImmutableList.of(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)));
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
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
        final Mqtt3SubAck suback = Mqtt3SubAckView.of(Mqtt3SubAckView.delegate(1,
                ImmutableList.of(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)));
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(asyncDelegate.unsubscribe(any(Mqtt3Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));


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
        final Mqtt3SubAck suback = Mqtt3SubAckView.of(Mqtt3SubAckView.delegate(1,
                ImmutableList.of(Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_0,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_1,
                        Mqtt3SubAckReturnCode.SUCCESS_MAXIMUM_QOS_2)));
        when(subscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic2", "topic3"});
        when(subscribeOptions.getQos()).thenReturn(new MqttQos[]{
                MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.EXACTLY_ONCE});
        when(asyncDelegate.subscribe(any(Mqtt3Subscribe.class), any(SubscribeMqtt3PublishCallback.class))).thenReturn(
                CompletableFuture.completedFuture(suback));

        final UnsubscribeOptions unsubscribeOptions = mock(UnsubscribeOptions.class);
        when(unsubscribeOptions.getTopics()).thenReturn(new String[]{"topic1", "topic3"});
        when(asyncDelegate.unsubscribe(any(Mqtt3Unsubscribe.class))).thenReturn(CompletableFuture.completedFuture(null));


        client.subscribe(subscribeOptions);
        final List<MqttTopicFilter> subscribedTopics = client.getSubscribedTopics();
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic1"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic2"));
        assertThat(subscribedTopics).contains(MqttTopicFilter.of("topic3"));

        client.unsubscribe(unsubscribeOptions);
        assertThat(client.getSubscribedTopics()).containsExactly(MqttTopicFilter.of("topic2"));
    }

}
