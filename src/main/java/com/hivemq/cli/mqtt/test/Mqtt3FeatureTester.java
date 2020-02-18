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
package com.hivemq.cli.mqtt.test;

import com.google.common.base.Strings;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3PubAckException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient.*;

public class Mqtt3FeatureTester {

    private final int LONG_TIME_OUT = 10;
    private final int SHORT_TIME_OUT = 2;

    private int maxTopicLength = -1;
    private final String host;
    private final int port;
    private final String username;
    private final ByteBuffer password;
    private final MqttClientSslConfig sslConfig;

    public Mqtt3FeatureTester(final @NotNull String host,
                              final @NotNull Integer port,
                              final @Nullable String username,
                              final @Nullable ByteBuffer password,
                              final @Nullable MqttClientSslConfig sslConfig) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sslConfig = sslConfig;
    }

    // Test methods

    public @Nullable Mqtt3ConnAck testConnect() {
        //TODO Wrap with object instead of Mqtt3Connack
        final Mqtt3Client client = buildClient();

        try {
            return client.toAsync().connect().get(LONG_TIME_OUT, TimeUnit.SECONDS);
        }
        catch (final Mqtt3ConnAckException ex) { return ex.getMqttMessage(); }
        catch (final Exception ex) { return null; }
        finally {
            if (client.getConfig().getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }

    public @NotNull WildcardSubscriptionsTestResult testWildcardSubscriptions() {
        final WildcardTestResult plusWildcardResult = testWildcard("+", "test");
        final WildcardTestResult hashWildcardResult = testWildcard("#", "test/subtopic");

        return new WildcardSubscriptionsTestResult(plusWildcardResult, hashWildcardResult);
    }

    public @NotNull RetainTestResult testRetain() {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String topic = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        publisher.toBlocking().connect();

        try {
            publisher.toBlocking().publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .retain(true)
                .payload("RETAIN".getBytes())
                .send();
        }
        catch (final Exception ex) { return RetainTestResult.PUBLISH_FAILED; }

        subscriber.toBlocking().connect();

        subscriber.toAsync().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    if (publish.isRetain()) {
                        countDownLatch.countDown();
                    }
                })
                .send()
                .join();

        try { countDownLatch.await(LONG_TIME_OUT, TimeUnit.SECONDS); }
        catch (final InterruptedException ex) {
            // TODO Log
        }

        return countDownLatch.getCount() == 0 ? RetainTestResult.OK : RetainTestResult.TIME_OUT;
    }

    public @NotNull QosTestResult testQos(final @NotNull MqttQos qos, final int tries) {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String topic = generateTopicUUID(maxTopicLength);
        final byte[] payload = qos.toString().getBytes();

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        final CountDownLatch countDownLatch = new CountDownLatch(tries);
        final AtomicInteger totalReceived = new AtomicInteger(0);

        subscriber.toAsync().subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {
                    if (publish.getQos() == qos
                            && Arrays.equals(publish.getPayloadAsBytes(), payload)) {
                        totalReceived.incrementAndGet();
                        countDownLatch.countDown();
                    }
                })
                .send()
                .join();

        final long before = System.nanoTime();

        for (int i = 0; i < tries; i++) {
            publisher.toAsync().publishWith()
                    .topic(topic)
                    .qos(qos)
                    .payload(payload)
                    .send();
        }

        try { countDownLatch.await(LONG_TIME_OUT, TimeUnit.SECONDS); }
        catch (InterruptedException e) { e.printStackTrace(); }

        final long after = System.nanoTime();
        final long timeToComplete = after - before;

        return new QosTestResult(totalReceived.get(), timeToComplete);
    }

    public int testPayloadSize(final int maxSize) {
        final Mqtt3Client client = buildClient();
        final String topic = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final Mqtt3Publishes publishes = client.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);
        final String oneByte = "a";
        final AtomicInteger top = new AtomicInteger(maxSize);
        final AtomicInteger bottom = new AtomicInteger(0);
        int mid = top.get() / 2;

        client.toBlocking().connect();

        client.toBlocking().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send();

        // Binary search the payload size
        while (bottom.get() <= top.get()) {
            final String currentPayload = Strings.repeat(oneByte, mid);
            final Mqtt3Publish publish = Mqtt3Publish.builder()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(currentPayload.getBytes())
                    .build();

            try {
                client.toBlocking().publish(publish);
                int finalMid = mid;
                publishes.receive(5, TimeUnit.SECONDS).ifPresent(mqtt3Publish -> {
                    if (Arrays.equals(mqtt3Publish.getPayloadAsBytes(), currentPayload.getBytes())) {
                        bottom.set(finalMid + 1);
                    }
                    else { top.set(finalMid - 1); }
                });
            }
            catch (final Mqtt3PubAckException pubAckEx) { top.set(mid - 1); }
            catch (final InterruptedException e) { e.printStackTrace(); }

            mid = (bottom.get() + top.get()) / 2;
        }
        client.toBlocking().disconnect();
        return mid;
    }

    public int testTopicLength() {
        final Mqtt3Client client = buildClient();
        client.toBlocking().connect();
        final String oneByte = "a";
        int top = 65535;
        int bottom = 0;
        int mid = top / 2;

        // Binary search the right topic length
        while (bottom <= top) {
            final String currentTopicName = Strings.repeat(oneByte, mid);

            try {
                Mqtt3Publish publish = Mqtt3Publish.builder()
                        .topic(currentTopicName)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .build();

                client.toBlocking().publish(publish);

                bottom = mid + 1;
            }
            catch (final Mqtt3PubAckException pubAckEx) { top = mid - 1; }
            mid = (bottom + top) / 2;
        }

        client.toBlocking().disconnect();

        return mid;
    }

    public int testClientIdLength() {
        final Mqtt3Client client = buildClient();
        final String oneByte = "a";
        final Mqtt3ClientBuilder mqtt3ClientBuilder = Mqtt3Client.builder()
                .serverHost(client.getConfig().getServerHost())
                .serverPort(client.getConfig().getServerPort());
        int top = 65535;
        int bottom = 0;
        int mid = top / 2;

        // Binary search the right client id length
        while (bottom <= top) {
            final String currentIdentifier = Strings.repeat(oneByte, mid);
            final Mqtt3Client currClient = getClientBuilder()
                    .identifier(currentIdentifier)
                    .build();

            try {
                final Mqtt3ConnAck connAck = currClient.toBlocking().connect();
                if (connAck.getReturnCode() == Mqtt3ConnAckReturnCode.SUCCESS) { bottom = mid + 1; }
                else { top = mid - 1; }
            }
            catch (final Mqtt3ConnAckException connAckEx) { top = mid - 1; }

            if (currClient.getConfig().getState().isConnected()) { currClient.toBlocking().disconnect(); }
            mid = (bottom + top) / 2;
        }

        return mid;
    }

    private @NotNull WildcardTestResult testWildcard(final String subscribeWildcardTopic, final String publishTopic) {
        final Mqtt3Client client = buildClient();
        final String topic = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final String subscribeToTopic = topic + "/" + subscribeWildcardTopic;
        final String publishToTopic = topic + "/" + publishTopic;
        final byte[] payload = "WILDCARD_TEST".getBytes();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Consumer<Mqtt3Publish> publishCallback = publish -> {
            if (Arrays.equals(publish.getPayloadAsBytes(), payload)) { countDownLatch.countDown(); }
        };

        client.toBlocking().connect();

        try {
            client.toAsync().subscribeWith()
                    .topicFilter(subscribeToTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback(publishCallback)
                    .send()
                    .join();
        }
        catch (final Exception ex) { return WildcardTestResult.SUBSCRIBE_FAILED; }

        try {
            client.toBlocking().publishWith()
                    .topic(publishToTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(payload)
                    .send();
        }
        catch (final Exception ex) { return WildcardTestResult.PUBLISH_FAILED; }

        try {
            countDownLatch.await(SHORT_TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Log
        }

        if (countDownLatch.getCount() == 0) { return WildcardTestResult.OK; }
        else { return WildcardTestResult.TIME_OUT; }
    }
    
    public @NotNull String testSpecialAsciiChars() {
        final String ASCII = " !\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
        final StringBuilder unsupportedChars = new StringBuilder();

        for (int i = 0; i < ASCII.length(); i++) {
            final String currChar = String.valueOf(ASCII.charAt(i));
            final Mqtt3Client client = getClientBuilder()
                    .identifier(currChar)
                    .build();

            try { client.toBlocking().connect(); }
            catch (final Exception ex) { unsupportedChars.append(currChar); }

            if (client.getConfig().getState().isConnected()) { client.toBlocking().disconnect(); }
        }

        return unsupportedChars.toString();
    }

    // Getter / Setter

    public void setMaxTopicLength(final int topicLength) { maxTopicLength = topicLength; }

    // Helpers

    private @NotNull Mqtt3Client buildClient() { return getClientBuilder().build(); }

    private @NotNull Mqtt3ClientBuilder getClientBuilder() {
        final Mqtt3ClientBuilder mqtt3ClientBuilder = Mqtt3Client.builder()
                .serverHost(host)
                .serverPort(port)
                .simpleAuth(buildAuth());

        if (sslConfig != null) {
            mqtt3ClientBuilder.sslConfig(sslConfig);
        }

        return mqtt3ClientBuilder;
    }

    private @Nullable  Mqtt3SimpleAuth buildAuth() {
        if (username != null && password != null) {
            return Mqtt3SimpleAuth.builder()
                    .username(username)
                    .password(password)
                    .build();
        }
        else if (username != null) {
            Mqtt3SimpleAuth.builder()
                    .username(username)
                    .build();
        }
        else if (password != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
        return null;
    }

    private @NotNull String generateTopicUUID() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.replace("-","");
    }

    private @NotNull String generateTopicUUID(final int maxLength) {
        if (maxLength == -1) return generateTopicUUID();
        else return generateTopicUUID().substring(0, maxLength);
    }
}


