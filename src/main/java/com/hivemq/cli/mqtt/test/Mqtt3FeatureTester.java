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
import com.hivemq.cli.mqtt.test.results.*;
import com.hivemq.cli.utils.TopicUtils;
import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3PubAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient.Mqtt3Publishes;

public class Mqtt3FeatureTester {

    private static final String ONE_BYTE = "a";
    private static final int MAX_TOPIC_LENGTH = 65535;
    private static final int MAX_CLIENT_ID_LENGTH = 65535;

    private int maxTopicLength = -1;
    private final String host;
    private final int port;
    private final String username;
    private final ByteBuffer password;
    private final MqttClientSslConfig sslConfig;
    private final int timeOut;
    private int maxClientIdLength = -1;
    private MqttQos maxQos = MqttQos.AT_MOST_ONCE;

    public Mqtt3FeatureTester(final @NotNull String host,
                              final @NotNull Integer port,
                              final @Nullable String username,
                              final @Nullable ByteBuffer password,
                              final @Nullable MqttClientSslConfig sslConfig,
                              final int timeOut) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sslConfig = sslConfig;
        this.timeOut = timeOut;
    }

    // Test methods

    public @Nullable Mqtt3ConnAck testConnect() throws Exception {
        final Mqtt3Client client = buildClient();

        try {
            final Mqtt3ConnAck connAck = client.toBlocking().connect();
            disconnectIfConnected(client);
            return connAck;
        } catch (final Mqtt3ConnAckException ex) {
            return ex.getMqttMessage();
        }
    }

    public @NotNull WildcardSubscriptionsTestResult testWildcardSubscriptions() {
        final TestResult plusWildcardResult = testWildcard("+", "test");
        final TestResult hashWildcardResult = testWildcard("#", "test/subtopic");

        return new WildcardSubscriptionsTestResult(plusWildcardResult, hashWildcardResult);
    }

    public @NotNull SharedSubscriptionTestResult testSharedSubscription() {
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final String sharedTopic = "$share/" + UUID.randomUUID().toString().replace("-", "") + "/" + topic;
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client sharedSubscriber1 = buildClient();
        final Mqtt3Client sharedSubscriber2 = buildClient();
        final Mqtt3Subscribe sharedSubscribe = Mqtt3Subscribe.builder()
                .topicFilter(sharedTopic)
                .qos(maxQos)
                .build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        publisher.toBlocking().connect();
        sharedSubscriber1.toBlocking().connect();
        sharedSubscriber2.toBlocking().connect();

        try {
            sharedSubscriber1.toBlocking().subscribe(sharedSubscribe);
            sharedSubscriber2.toBlocking().subscribe(sharedSubscribe);
        } catch (final Mqtt3SubAckException ex) {
            Logger.error(ex,"Could not subscribe to topic {} with qos {}", sharedTopic, maxQos);
            disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);
            return SharedSubscriptionTestResult.SUBSCRIBE_FAILED;
        }

        long startTime = 0;

        sharedSubscriber1.toAsync().subscribeWith()
                .topicFilter(sharedTopic)
                .qos(maxQos)
                .callback(publish -> {
                    if (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    } else {
                        atomicBoolean.set(true);
                    }
                })
                .send()
                .join();

        sharedSubscriber2.toAsync().subscribeWith()
                .topicFilter(sharedTopic)
                .qos(maxQos)
                .callback(publish -> {
                    if (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    } else {
                        atomicBoolean.set(true);
                    }
                })
                .send()
                .join();

        try {
            publisher.toBlocking().publishWith()
                    .topic(topic)
                    .payload("test".getBytes())
                    .qos(maxQos)
                    .send();
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            Logger.error(e, "Could not publish to topic " + sharedTopic);
            disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);
            return SharedSubscriptionTestResult.PUBLISH_FAILED;
        }

        boolean timedOut;
        long timeToReceive = 0;

        try {
            timedOut = !countDownLatch.await(timeOut, TimeUnit.SECONDS);
            timeToReceive = System.currentTimeMillis() - startTime;
        } catch (InterruptedException e) {
            Logger.error(e, "Waiting for subscribers to receive shared publishes interrupted");
            disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);
            return SharedSubscriptionTestResult.INTERRUPTED;
        }

        if (timedOut) {
            return SharedSubscriptionTestResult.TIME_OUT;
        }

        try {
            Thread.sleep(100 + timeToReceive);
        } catch (InterruptedException e) {
            Logger.error(e,"Waiting additional time for second subscriber interrupted");
            disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);
            return SharedSubscriptionTestResult.INTERRUPTED;
        }

        disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);

        if (atomicBoolean.get()) {
            return SharedSubscriptionTestResult.NOT_SHARED;
        } else {
            return SharedSubscriptionTestResult.OK;
        }

    }

    public @NotNull TestResult testRetain() {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        publisher.toBlocking().connect();

        try {
            publisher.toBlocking().publishWith()
                    .topic(topic)
                    .qos(maxQos)
                    .retain(true)
                    .payload("RETAIN".getBytes())
                    .send();
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3PubAckException)) {
                Logger.error(ex, "Retained publish failed");
            }
            disconnectIfConnected(publisher);
            return TestResult.PUBLISH_FAILED;
        }
        finally {
            disconnectIfConnected(publisher);
        }

        subscriber.toBlocking().connect();

        try {
            subscriber.toAsync().subscribeWith()
                    .topicFilter(topic)
                    .qos(maxQos)
                    .callback(publish -> {
                        if (publish.isRetain()) {
                            countDownLatch.countDown();
                        }
                    })
                    .send()
                    .join();
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3SubAckException)) {
                Logger.error(ex, "Retained subscribe failed");
            }
            disconnectIfConnected(subscriber);
            return TestResult.SUBSCRIBE_FAILED;
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Logger.error(ex, "Interrupted while waiting for retained publish to arrive at subscriber");
        }

        disconnectIfConnected(publisher, subscriber);

        return countDownLatch.getCount() == 0 ? TestResult.OK : TestResult.TIME_OUT;
    }

    public @NotNull QosTestResult testQos(final @NotNull MqttQos qos, final int tries) {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String topic = TopicUtils.generateTopicUUID(maxTopicLength);
        final byte[] payload = qos.toString().getBytes();

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        final CountDownLatch countDownLatch = new CountDownLatch(tries);
        final AtomicInteger totalReceived = new AtomicInteger(0);

        try {
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
        } catch (final Exception ex) {
            Logger.error(ex, "Could not subscribe with QoS {}", qos.ordinal());
        }

        final long before = System.nanoTime();

        for (int i = 0; i < tries; i++) {
            try {
                publisher.toBlocking().publishWith()
                        .topic(topic)
                        .qos(qos)
                        .payload(payload)
                        .send();
            } catch (final Exception ex) {
                countDownLatch.countDown();
                Logger.error("Could not publish with QoS {}", qos.ordinal());
            }
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error("Interrupted while waiting for QoS {} publish to arrive at subscriber", qos.ordinal());
        }

        final long after = System.nanoTime();
        final long timeToComplete = after - before;

        disconnectIfConnected(publisher, subscriber);

        if (totalReceived.get() > 0 && qos.ordinal() > maxQos.ordinal()) {
            maxQos = qos;
        }

        return new QosTestResult(totalReceived.get(), timeToComplete);
    }

    public @NotNull PayloadTestResults testPayloadSize(final int maxSize) {
        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));

        final boolean maxTestSuccess = testPayload(topic, testResults, maxSize);
        if (maxTestSuccess) {
            return new PayloadTestResults(maxSize, testResults);
        } else { // Binary search the payload size
            int top = maxSize;
            int bottom = 0;
            int mid = -1;
            while (bottom <= top) {
                mid = (bottom + top) / 2;
                final boolean success = testPayload(topic, testResults, mid);
                if (success) {
                    bottom = mid + 1;
                } else {
                    top = mid - 1;
                }
            }

            return new PayloadTestResults(mid, testResults);
        }
    }

    private boolean testPayload(final @NotNull String topic,
                                final @NotNull List<Tuple<Integer, TestResult>> testResults,
                                final int payloadSize) {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String currentPayload = Strings.repeat(ONE_BYTE, payloadSize);
        final Mqtt3Publish publish = Mqtt3Publish.builder()
                .topic(topic)
                .qos(maxQos)
                .payload(currentPayload.getBytes())
                .build();

        subscriber.toBlocking().connect();
        subscriber.toBlocking().subscribeWith()
                .topicFilter(topic)
                .qos(maxQos)
                .send();
        final Mqtt3Publishes publishes = subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);

        try {
            if (!publisher.getState().isConnectedOrReconnect()) {
                publisher.toBlocking().connect();
            }
            publisher.toBlocking().publish(publish);
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3PubAckException)) {
                Logger.error(ex, "Publish with payload of size {} bytes failed", currentPayload.getBytes().length);
            }
            testResults.add(new Tuple<>(payloadSize, TestResult.PUBLISH_FAILED));
            disconnectIfConnected(publisher, subscriber);
            return false;
        }

        try {
            final Optional<Mqtt3Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
            if (!receive.isPresent()) {
                testResults.add(new Tuple<>(payloadSize, TestResult.TIME_OUT));
                disconnectIfConnected(publisher, subscriber);
                return false;
            } else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentPayload.getBytes())) {
                testResults.add(new Tuple<>(payloadSize, TestResult.WRONG_PAYLOAD));
                disconnectIfConnected(publisher, subscriber);
                return false;
            }
        } catch (InterruptedException e) {
            Logger.error(e, "Interrupted while waiting for subscriber to receive payload with length {} bytes", currentPayload.getBytes().length);
            testResults.add(new Tuple<>(payloadSize, TestResult.INTERRUPTED));
            disconnectIfConnected(publisher, subscriber);
            return false;
        }

        disconnectIfConnected(publisher, subscriber);

        testResults.add(new Tuple<>(payloadSize, TestResult.OK));
        return true;
    }

    public @NotNull TopicLengthTestResults testTopicLength() {
        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();


        final boolean maxTopicLengthSuccess = testTopic(testResults, MAX_TOPIC_LENGTH);
        if (maxTopicLengthSuccess) {
            return new TopicLengthTestResults(MAX_TOPIC_LENGTH, testResults);
        } else { // Binary search the right topic length
            int top = MAX_TOPIC_LENGTH;
            int bottom = 0;
            int mid = -1;

            while (bottom <= top) {
                mid = (bottom + top) / 2;
                if (mid == 0) return new TopicLengthTestResults(0, testResults);
                final boolean success = testTopic(testResults, mid);
                if (success) {
                    bottom = mid + 1;
                } else {
                    top = mid - 1;
                }
            }

            setMaxTopicLength(mid);

            return new TopicLengthTestResults(mid, testResults);
        }
    }

    private boolean testTopic(final @NotNull List<Tuple<Integer, TestResult>> testResults,
                              final int topicSize) {
        final Mqtt3Client publisher = buildClient();
        final Mqtt3Client subscriber = buildClient();
        final String currentTopicName = Strings.repeat(ONE_BYTE, topicSize);
        final Mqtt3Publish publish = Mqtt3Publish.builder()
                .topic(currentTopicName)
                .qos(maxQos)
                .payload(currentTopicName.getBytes())
                .build();
        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .topicFilter(currentTopicName)
                .qos(maxQos)
                .build();

        subscriber.toBlocking().connect();
        final Mqtt3Publishes publishes = subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);

        // Test subscribe to topic
        try {
            subscriber.toBlocking().subscribe(subscribe);
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3SubAckException)) {
                Logger.error(ex, "Subscribe to topic of length {} bytes failed", currentTopicName.getBytes().length);
            }
            testResults.add(new Tuple<>(topicSize, TestResult.SUBSCRIBE_FAILED));
            return false;
        }

        // Test publish to topic
        try {
            publisher.toBlocking().connect();
            publisher.toBlocking().publish(publish);
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3PubAckException)) {
                Logger.error(ex, "Publish to topic of length {} failed", currentTopicName.getBytes().length);
            }
            testResults.add(new Tuple<>(topicSize, TestResult.PUBLISH_FAILED));
            return false;
        } finally {
          disconnectIfConnected(publisher);
        }

        // Subscriber retrieves payload
        try {
            final Optional<Mqtt3Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
            if (!receive.isPresent()) {
                testResults.add(new Tuple<>(topicSize, TestResult.TIME_OUT));
                return false;
            } else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentTopicName.getBytes())) {
                testResults.add(new Tuple<>(topicSize, TestResult.WRONG_PAYLOAD));
                return false;
            }
        } catch (InterruptedException e) {
            Logger.error(e, "Interrupted while waiting to receive publish to topic with {} bytes", currentTopicName.getBytes().length);
            testResults.add(new Tuple<>(topicSize, TestResult.INTERRUPTED));
            return false;
        }
        finally {
            disconnectIfConnected(subscriber);
        }

        // Everything successful
        testResults.add(new Tuple<>(topicSize, TestResult.OK));
        return true;
    }

    public @NotNull ClientIdLengthTestResults testClientIdLength() {
        final List<Tuple<Integer, String>> connectResults = new LinkedList<>();

        final boolean maxClientIdSuccess = testClientIdLength(connectResults, MAX_CLIENT_ID_LENGTH);
        if (maxClientIdSuccess) {
            return new ClientIdLengthTestResults(MAX_CLIENT_ID_LENGTH, connectResults);
        }
        else { // Binary search the right client id length
            int top = MAX_CLIENT_ID_LENGTH;
            int bottom = 0;
            int mid = -1;
            while (bottom <= top) {
                mid = (bottom + top) / 2;
                final boolean success = testClientIdLength(connectResults, mid);
                if (success) {
                    bottom = mid + 1;
                } else {
                    top = mid -1;
                }
            }

            maxClientIdLength = mid;
            return new ClientIdLengthTestResults(mid, connectResults);
        }
    }

    private boolean testClientIdLength(final @NotNull List<Tuple<Integer, String>> connectResults,
                                       final int clientIdLength) {
        final String currentIdentifier = Strings.repeat(ONE_BYTE, clientIdLength);
        final Mqtt3Client currClient = getClientBuilder()
                .identifier(currentIdentifier)
                .build();

        try {
            final Mqtt3ConnAck connAck = currClient.toBlocking().connect();
            connectResults.add(new Tuple<>(clientIdLength, connAck.getReturnCode().toString()));
            if (connAck.getReturnCode() != Mqtt3ConnAckReturnCode.SUCCESS) {
                return false;
            }
        } catch (final Mqtt3ConnAckException connAckEx) {
            connectResults.add(new Tuple<>(clientIdLength, connAckEx.getMqttMessage().getReturnCode().toString()));
            return false;
        }
        catch (final Exception ex) {
                Logger.error(ex, "Connect with client id length {} bytes",
                        currClient.getConfig().getClientIdentifier()
                                .map(id -> id.toString().getBytes().length).orElse(0));
            connectResults.add(new Tuple<>(clientIdLength, "UNDEFINED_FAILURE"));
            return false;
        }
        finally {
            disconnectIfConnected(currClient);
        }

        return true;

    }

    private @NotNull TestResult testWildcard(final String subscribeWildcardTopic, final String publishTopic) {
        final Mqtt3Client subscriber = buildClient();
        final Mqtt3Client publisher = buildClient();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final String subscribeToTopic = topic + "/" + subscribeWildcardTopic;
        final String publishToTopic = topic + "/" + publishTopic;
        final byte[] payload = "WILDCARD_TEST".getBytes();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Consumer<Mqtt3Publish> publishCallback = publish -> {
            if (Arrays.equals(publish.getPayloadAsBytes(), payload)) {
                countDownLatch.countDown();
            }
        };

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        try {
            subscriber.toAsync().subscribeWith()
                    .topicFilter(subscribeToTopic)
                    .qos(maxQos)
                    .callback(publishCallback)
                    .send()
                    .join();
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3SubAckException)) {
                Logger.error(ex, "Subscribe to wildcard topic '{}' failed", subscribeToTopic);
            }
            return TestResult.SUBSCRIBE_FAILED;
        }

        try {
            publisher.toBlocking().publishWith()
                    .topic(publishToTopic)
                    .qos(maxQos)
                    .payload(payload)
                    .send();
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt3PubAckException)) {
                Logger.error(ex, "Publish to topic '{}' failed", publishToTopic);
            }
            return TestResult.PUBLISH_FAILED;
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error(e,
                    "Interrupted while subscription to '{}' receives publish to '{}'",
                    subscribeToTopic,
                    publishToTopic);
        }

        disconnectIfConnected(publisher, subscriber);

        return countDownLatch.getCount() == 0 ? TestResult.OK : TestResult.TIME_OUT;
    }

    public @NotNull AsciiCharsInClientIdTestResults testAsciiCharsInClientId() {
        final String ASCII = " !\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
        final List<Tuple<Character, String>> connectResults = new LinkedList<>();

        boolean allSuccess = false;
        final Mqtt3Client client = getClientBuilder()
                .identifier(ASCII)
                .build();

        if (ASCII.length() <= maxClientIdLength) {
            try {
                client.toBlocking().connect();
                allSuccess = true;
            }
            catch (Exception ex) {
                Logger.error(ex,"Could not connect with Client ID '" + ASCII + "'");
            }
            finally {
                disconnectIfConnected(client);
            }
        }

        if (allSuccess) {
            return new AsciiCharsInClientIdTestResults(connectResults);
        }
        else {
            for (int i = 0; i < ASCII.length(); i++) {
                testAsciiChar(connectResults, ASCII.charAt(i));
            }
            return new AsciiCharsInClientIdTestResults(connectResults);
        }
    }

        private void testAsciiChar(final @NotNull  List<Tuple<Character, String>> connectResults,
                    final char asciiChar) {
            final Mqtt3Client client = getClientBuilder()
                    .identifier(String.valueOf(asciiChar))
                    .build();

            try {
                client.toBlocking().connect();
            } catch (final Mqtt3ConnAckException ex) {
                connectResults.add(new Tuple<>(asciiChar, ex.getMqttMessage().getReturnCode().toString()));
            } catch (final Exception ex) {
                Logger.error("Connect with Ascii char '{}' failed", asciiChar);
                connectResults.add(new Tuple<>(asciiChar, null));
            }

            disconnectIfConnected(client);
        }

    // Getter / Setter

    public void setMaxTopicLength(final int topicLength) {
        maxTopicLength = topicLength;
    }

    // Helpers

    private @NotNull Mqtt3Client buildClient() {
        return getClientBuilder().build();
    }

    private @NotNull Mqtt3ClientBuilder getClientBuilder() {

        return Mqtt3Client.builder()
                .serverHost(host)
                .serverPort(port)
                .simpleAuth(buildAuth())
                .sslConfig(sslConfig);
    }

    private @Nullable Mqtt3SimpleAuth buildAuth() {
        if (username != null && password != null) {
            return Mqtt3SimpleAuth.builder()
                    .username(username)
                    .password(password)
                    .build();
        } else if (username != null) {
            Mqtt3SimpleAuth.builder()
                    .username(username)
                    .build();
        } else if (password != null) {
            throw new IllegalArgumentException("Password-Only Authentication is not allowed in MQTT 3");
        }
        return null;
    }

    private void disconnectIfConnected(final @NotNull Mqtt3Client... clients) {
        for (Mqtt3Client client : clients) {
            if (client.getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }
}


