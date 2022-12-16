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

package com.hivemq.cli.mqtt.test;

import com.google.common.base.Strings;
import com.hivemq.cli.mqtt.test.results.AsciiCharsInClientIdTestResults;
import com.hivemq.cli.mqtt.test.results.ClientIdLengthTestResults;
import com.hivemq.cli.mqtt.test.results.PayloadTestResults;
import com.hivemq.cli.mqtt.test.results.QosTestResult;
import com.hivemq.cli.mqtt.test.results.SharedSubscriptionTestResult;
import com.hivemq.cli.mqtt.test.results.TestResult;
import com.hivemq.cli.mqtt.test.results.TopicLengthTestResults;
import com.hivemq.cli.mqtt.test.results.WildcardSubscriptionsTestResult;
import com.hivemq.cli.utils.TopicUtils;
import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

public class Mqtt5FeatureTester {

    private static final @NotNull String ONE_BYTE = "a";
    private static final int MAX_TOPIC_LENGTH = 65_535;
    private static final int MAX_CLIENT_ID_LENGTH = 65_535;

    private final @NotNull String host;
    private final int port;
    private final @Nullable String username;
    private final @Nullable ByteBuffer password;
    private final @Nullable MqttClientSslConfig sslConfig;
    private final int timeOut;

    private int maxTopicLength = -1;
    private int maxClientIdLength = -1;
    private @NotNull MqttQos maxQos = MqttQos.AT_MOST_ONCE;

    public Mqtt5FeatureTester(
            final @NotNull String host,
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
    public @Nullable Mqtt5ConnAck testConnect() {
        Logger.debug("Testing connect");

        final Mqtt5Client mqtt5Client = buildClient();

        try {
            final Mqtt5ConnAck connAck = mqtt5Client.toBlocking().connect();
            Logger.debug("Received {}", connAck);
            disconnectIfConnected(mqtt5Client);
            return connAck;
        } catch (final Mqtt5ConnAckException connAckEx) {
            Logger.debug(connAckEx, "Failed to connect MQTT 5 client");
            disconnectIfConnected(mqtt5Client);
            return connAckEx.getMqttMessage();
        }
    }

    public @NotNull SharedSubscriptionTestResult testSharedSubscription() {
        Logger.debug("Testing shared subscriptions");

        final String topic =
                (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final String sharedTopic = "$share/" + UUID.randomUUID().toString().replace("-", "") + "/" + topic;
        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client sharedSubscriber1 = buildClient();
        final Mqtt5Client sharedSubscriber2 = buildClient();
        final Mqtt5Subscribe sharedSubscribe = Mqtt5Subscribe.builder().topicFilter(sharedTopic).qos(maxQos).build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        publisher.toBlocking().connect();
        sharedSubscriber1.toBlocking().connect();
        sharedSubscriber2.toBlocking().connect();

        try {
            sharedSubscriber1.toBlocking().subscribe(sharedSubscribe);
            sharedSubscriber2.toBlocking().subscribe(sharedSubscribe);
        } catch (final Mqtt5SubAckException ex) {
            Logger.error(ex, "Could not subscribe to topic {} with qos {}", sharedTopic, maxQos);
            disconnectIfConnected(sharedSubscriber1, sharedSubscriber2);
            return SharedSubscriptionTestResult.SUBSCRIBE_FAILED;
        }

        Logger.trace("Subscribing first subscriber to shared topic {} with qos {}", sharedTopic, maxQos);
        sharedSubscriber1.toAsync().subscribeWith().topicFilter(sharedTopic).qos(maxQos).callback(publish -> {
            if (countDownLatch.getCount() != 0) {
                countDownLatch.countDown();
            } else {
                atomicBoolean.set(true);
            }
        }).send().join();

        Logger.trace("Subscribing second subscriber to shared topic {} with qos {}", sharedTopic, maxQos);
        sharedSubscriber2.toAsync().subscribeWith().topicFilter(sharedTopic).qos(maxQos).callback(publish -> {
            if (countDownLatch.getCount() != 0) {
                countDownLatch.countDown();
            } else {
                atomicBoolean.set(true);
            }
        }).send().join();

        final long startTime;

        try {
            Logger.trace("Publishing to shared topic {} with qos {}", sharedTopic, maxQos);
            publisher.toBlocking().publishWith().topic(topic).payload("test".getBytes()).qos(maxQos).send();
            startTime = System.currentTimeMillis();
        } catch (final Exception e) {
            Logger.error(e, "Could not publish to topic " + sharedTopic);
            disconnectIfConnected(publisher, sharedSubscriber1, sharedSubscriber1);
            return SharedSubscriptionTestResult.PUBLISH_FAILED;
        }

        final boolean timedOut;
        final long timeToReceive;

        try {
            timedOut = !countDownLatch.await(timeOut, TimeUnit.SECONDS);
            timeToReceive = System.currentTimeMillis() - startTime;
        } catch (final InterruptedException e) {
            Logger.error(e, "Waiting for subscribers to receive shared publishes interrupted");
            disconnectIfConnected(publisher, sharedSubscriber1, sharedSubscriber1);
            return SharedSubscriptionTestResult.INTERRUPTED;
        }

        if (timedOut) {
            Logger.debug("Timed out while waiting for shared subscription publish");
            disconnectIfConnected(publisher, sharedSubscriber1, sharedSubscriber1);
            return SharedSubscriptionTestResult.TIME_OUT;
        }

        try {
            Thread.sleep(100 + timeToReceive);
        } catch (final InterruptedException e) {
            Logger.error(e, "Waiting additional time for second subscriber interrupted");
            disconnectIfConnected(publisher, sharedSubscriber1, sharedSubscriber1);
            return SharedSubscriptionTestResult.INTERRUPTED;
        }

        disconnectIfConnected(publisher, sharedSubscriber1, sharedSubscriber1);

        final boolean result = atomicBoolean.get();

        final SharedSubscriptionTestResult testResult =
                result ? SharedSubscriptionTestResult.NOT_SHARED : SharedSubscriptionTestResult.OK;

        Logger.debug("Result of testing shared subscriptions: {}", testResult);

        return testResult;
    }

    public @NotNull QosTestResult testQos(final @NotNull MqttQos qos, final int tries) {
        Logger.debug("Testing qos {} with {} tries", qos, tries);

        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
        final String topic = TopicUtils.generateTopicUUID(maxTopicLength);
        final byte[] payload = qos.toString().getBytes();

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        final CountDownLatch countDownLatch = new CountDownLatch(tries);
        final AtomicInteger totalReceived = new AtomicInteger(0);

        try {
            Logger.trace("Subscribing to topic {} with qos {}", topic, qos);
            subscriber.toAsync().subscribeWith().topicFilter(topic).qos(qos).callback(publish -> {
                Logger.trace("Subscriber received {}", publish);
                if (publish.getQos() == qos && Arrays.equals(publish.getPayloadAsBytes(), payload)) {
                    totalReceived.incrementAndGet();
                    countDownLatch.countDown();
                }
            }).send().join();
        } catch (final Exception ex) {
            Logger.error(ex, "Could not subscribe with QoS {}", qos.ordinal());
        }

        final long before = System.nanoTime();

        for (int i = 0; i < tries; i++) {
            try {
                Logger.trace("Publishing message {} to topic {} with qos {}",
                        new String(payload, StandardCharsets.UTF_8),
                        topic,
                        qos);
                publisher.toAsync().publishWith().topic(topic).qos(qos).payload(payload).send();
            } catch (final Exception ex) {
                countDownLatch.countDown();
                Logger.error("Could not publish with QoS {}", qos.ordinal());
            }
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Logger.error("Interrupted while waiting for QoS {} publish to arrive at subscriber", qos.ordinal());
        }

        final long after = System.nanoTime();
        final long timeToComplete = after - before;

        disconnectIfConnected(publisher, subscriber);

        if (totalReceived.get() > 0 && qos.ordinal() > maxQos.ordinal()) {
            Logger.trace("Setting maxQos from {} to {} for the next tests", maxQos, qos);
            maxQos = qos;
        }

        Logger.debug("Result of testing qos {}: Received {} / {} publishes", qos, totalReceived, tries);

        return new QosTestResult(totalReceived.get(), timeToComplete);
    }

    public @NotNull TestResult testRetain() {
        Logger.debug("Testing retained messages");

        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
        final String topic =
                (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        publisher.toBlocking().connect();

        try {
            Logger.trace("Publishing retained message '{}' to topic {} with qos {}", "RETAIN", topic, maxQos);
            publisher.toBlocking()
                    .publishWith()
                    .topic(topic)
                    .qos(maxQos)
                    .retain(true)
                    .payload("RETAIN".getBytes())
                    .send();
        } catch (final Exception ex) {
            Logger.error(ex, "Retained publish failed");
            disconnectIfConnected(publisher);
            return TestResult.PUBLISH_FAILED;
        }

        subscriber.toBlocking().connect();

        try {
            Logger.trace("Subscribing to topic {} with qos {}", topic, maxQos);
            subscriber.toAsync().subscribeWith().topicFilter(topic).qos(maxQos).callback(publish -> {
                Logger.trace("Subscriber received {}", publish);
                if (publish.isRetain()) {
                    countDownLatch.countDown();
                }
            }).send().join();
        } catch (final Exception ex) {
            Logger.error(ex, "Retained subscribe failed");
            disconnectIfConnected(publisher, subscriber);
            return TestResult.SUBSCRIBE_FAILED;
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Logger.error(ex, "Interrupted while waiting for retained publish to arrive at subscriber");
        }

        disconnectIfConnected(publisher, subscriber);

        final TestResult testResult = countDownLatch.getCount() == 0 ? TestResult.OK : TestResult.TIME_OUT;

        Logger.debug("Result of testing retained messages: {}", testResult);

        return testResult;
    }

    public @NotNull WildcardSubscriptionsTestResult testWildcardSubscriptions() {
        final TestResult plusWildcardResult = testWildcard("+", "test");
        final TestResult hashWildcardResult = testWildcard("#", "test/subtopic");

        return new WildcardSubscriptionsTestResult(plusWildcardResult, hashWildcardResult);
    }

    private @NotNull TestResult testWildcard(
            final @NotNull String subscribeWildcardTopic, final @NotNull String publishTopic) {
        Logger.debug("Testing wildcard {} on topic {}", subscribeWildcardTopic, publishTopic);

        final Mqtt5Client subscriber = buildClient();
        final Mqtt5Client publisher = buildClient();
        final String topic =
                (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final String subscribeToTopic = topic + "/" + subscribeWildcardTopic;
        final String publishToTopic = topic + "/" + publishTopic;
        final byte[] payload = "WILDCARD_TEST".getBytes();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Consumer<Mqtt5Publish> publishCallback = publish -> {
            if (Arrays.equals(publish.getPayloadAsBytes(), payload)) {
                countDownLatch.countDown();
            }
        };

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        try {
            Logger.trace("Subscribing to wildcard topic {} with qos {}", subscribeToTopic, maxQos);
            subscriber.toAsync()
                    .subscribeWith()
                    .topicFilter(subscribeToTopic)
                    .qos(maxQos)
                    .callback(publishCallback)
                    .send()
                    .join();
        } catch (final Exception ex) {
            Logger.error(ex, "Subscribe to wildcard topic '{}' failed", subscribeToTopic);
            disconnectIfConnected(subscriber, publisher);
            return TestResult.SUBSCRIBE_FAILED;
        }

        try {
            Logger.trace("Publishing to wildcard topic {} with qos {}", publishTopic, maxQos);
            publisher.toBlocking().publishWith().topic(publishToTopic).qos(maxQos).payload(payload).send();
        } catch (final Exception ex) {
            Logger.error(ex, "Publish to topic '{}' failed", publishToTopic);
            disconnectIfConnected(subscriber, publisher);
            return TestResult.PUBLISH_FAILED;
        }

        try {
            countDownLatch.await(timeOut, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Logger.error(e,
                    "Interrupted while subscription to '{}' receives publish to '{}'",
                    subscribeToTopic,
                    publishToTopic);
        }

        disconnectIfConnected(publisher, subscriber);

        return countDownLatch.getCount() == 0 ? TestResult.OK : TestResult.TIME_OUT;
    }

    public @NotNull PayloadTestResults testPayloadSize(final int maxSize) {
        Logger.debug("Testing payload size until max. payload size of {} bytes", maxSize);

        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();
        final String topic =
                (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));


        final boolean maxTestSuccess = testPayload(topic, testResults, maxSize);
        if (maxTestSuccess) {
            Logger.debug("Result of testing max. payload size: {} bytes", maxSize);
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

            Logger.debug("Result of testing max. payload size: {} bytes", mid);
            return new PayloadTestResults(mid, testResults);
        }
    }

    private boolean testPayload(
            final @NotNull String topic,
            final @NotNull List<Tuple<Integer, TestResult>> testResults,
            final int payloadSize) {
        Logger.debug("Testing payload with {} bytes", payloadSize);

        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
        final String currentPayload = Strings.repeat(ONE_BYTE, payloadSize);
        final Mqtt5Publish publish =
                Mqtt5Publish.builder().topic(topic).qos(maxQos).payload(currentPayload.getBytes()).build();

        subscriber.toBlocking().connect();

        Logger.trace("Subscribing to topic {} with qos {}", topic, maxQos);
        subscriber.toBlocking().subscribeWith().topicFilter(topic).qos(maxQos).send();
        final Mqtt5BlockingClient.Mqtt5Publishes publishes =
                subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);

        try {
            publisher.toBlocking().connect();
            Logger.trace("Publishing payload with {} bytes to topic {} with qos {}", payloadSize, topic, maxQos);
            publisher.toBlocking().publish(publish);
        } catch (final Exception ex) {
            if (!(ex instanceof Mqtt5PubAckException)) {
                Logger.error(ex, "Publish with payload of size {} bytes failed", currentPayload.getBytes().length);
            }
            disconnectIfConnected(subscriber, publisher);
            testResults.add(Tuple.of(payloadSize, TestResult.PUBLISH_FAILED));
            return false;
        }

        try {
            final Optional<Mqtt5Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
            if (!receive.isPresent()) {
                disconnectIfConnected(publisher, subscriber);
                Logger.debug("Timed out while waiting for publish with {} bytes", currentPayload.getBytes().length);
                testResults.add(Tuple.of(payloadSize, TestResult.TIME_OUT));
                return false;
            } else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentPayload.getBytes())) {
                disconnectIfConnected(publisher, subscriber);
                Logger.debug("Received wrong payload for publish with {} bytes", currentPayload.getBytes().length);
                testResults.add(Tuple.of(payloadSize, TestResult.WRONG_PAYLOAD));
                return false;
            }
        } catch (final InterruptedException e) {
            Logger.error(e,
                    "Interrupted while waiting for subscriber to receive payload with length {} bytes",
                    currentPayload.getBytes().length);
            testResults.add(Tuple.of(payloadSize, TestResult.INTERRUPTED));
            disconnectIfConnected(subscriber, publisher);
            return false;
        }

        disconnectIfConnected(subscriber, publisher);

        testResults.add(Tuple.of(payloadSize, TestResult.OK));
        return true;
    }

    public @NotNull TopicLengthTestResults testTopicLength() {
        Logger.debug("Testing topic length");

        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();

        final boolean maxTopicLengthSuccess = testTopic(testResults, MAX_TOPIC_LENGTH);
        if (maxTopicLengthSuccess) {
            Logger.debug("Result of testing max. topic length: {} bytes", MAX_TOPIC_LENGTH);
            return new TopicLengthTestResults(MAX_TOPIC_LENGTH, testResults);
        } else { // Binary search the right topic length
            int top = MAX_TOPIC_LENGTH;
            int bottom = 0;
            int mid = -1;

            while (bottom <= top) {
                mid = (bottom + top) / 2;
                final boolean success = testTopic(testResults, mid);
                if (success) {
                    bottom = mid + 1;
                } else {
                    top = mid - 1;
                }
            }

            Logger.debug("Result of testing max. topic length: {} bytes", mid);
            Logger.trace("Setting max. topic length to {} for the next tests", mid);
            setMaxTopicLength(mid);

            return new TopicLengthTestResults(mid, testResults);
        }
    }

    private boolean testTopic(final @NotNull List<Tuple<Integer, TestResult>> testResults, final int topicSize) {
        Logger.debug("Testing topic with length of {} bytes", topicSize);

        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
        final String currentTopicName = Strings.repeat(ONE_BYTE, topicSize);
        final Mqtt5Publish publish =
                Mqtt5Publish.builder().topic(currentTopicName).qos(maxQos).payload(currentTopicName.getBytes()).build();
        final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder().topicFilter(currentTopicName).qos(maxQos).build();

        subscriber.toBlocking().connect();
        final Mqtt5BlockingClient.Mqtt5Publishes publishes =
                subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);

        // Test subscribe to topic
        try {
            Logger.trace("Subscribing to topic with {} bytes with qos {}", topicSize, maxQos);
            subscriber.toBlocking().subscribe(subscribe);
        } catch (final Exception ex) {
            Logger.error(ex, "Subscribe to topic of length {} bytes failed", currentTopicName.getBytes().length);
            testResults.add(Tuple.of(topicSize, TestResult.SUBSCRIBE_FAILED));
            disconnectIfConnected(subscriber);
            return false;
        }

        // Test publish to topic
        try {
            publisher.toBlocking().connect();
            Logger.trace("Publishing to topic with {} bytes with qos {}", topicSize, maxQos);
            publisher.toBlocking().publish(publish);
        } catch (final Exception ex) {
            Logger.error(ex, "Publish to topic of length {} failed", currentTopicName.getBytes().length);
            testResults.add(Tuple.of(topicSize, TestResult.PUBLISH_FAILED));
            disconnectIfConnected(publisher, subscriber);
            return false;
        }

        // Subscriber retrieves payload
        try {
            final Optional<Mqtt5Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
            if (!receive.isPresent()) {
                Logger.debug("Timed out while waiting to receive a publish from topic {}", currentTopicName);
                testResults.add(Tuple.of(topicSize, TestResult.TIME_OUT));
                disconnectIfConnected(subscriber, publisher);
                return false;
            } else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentTopicName.getBytes())) {
                Logger.debug("Received wrong payload for publish to topic {}", currentTopicName);
                testResults.add(Tuple.of(topicSize, TestResult.WRONG_PAYLOAD));
                disconnectIfConnected(subscriber, publisher);
                return false;
            }
        } catch (final InterruptedException e) {
            Logger.error(e,
                    "Interrupted while waiting to receive publish to topic with {} bytes",
                    currentTopicName.getBytes().length);
            testResults.add(Tuple.of(topicSize, TestResult.INTERRUPTED));
            disconnectIfConnected(subscriber, publisher);
            return false;
        }

        disconnectIfConnected(subscriber, publisher);

        // Everything successful
        testResults.add(Tuple.of(topicSize, TestResult.OK));
        return true;
    }

    public @NotNull ClientIdLengthTestResults testClientIdLength() {
        Logger.debug("Testing max. client identifier length");

        final List<Tuple<Integer, String>> connectResults = new LinkedList<>();

        final boolean maxClientIdSuccess = testClientIdLength(connectResults, MAX_CLIENT_ID_LENGTH);
        if (maxClientIdSuccess) {
            maxClientIdLength = MAX_CLIENT_ID_LENGTH;
            Logger.debug("Result of testing max. client identifier length: {} bytes", MAX_CLIENT_ID_LENGTH);
            return new ClientIdLengthTestResults(MAX_CLIENT_ID_LENGTH, connectResults);
        } else { // Binary search the right client id length
            int top = MAX_CLIENT_ID_LENGTH;
            int bottom = 0;
            int mid = -1;
            while (bottom <= top) {
                mid = (bottom + top) / 2;
                final boolean success = testClientIdLength(connectResults, mid);
                if (success) {
                    bottom = mid + 1;
                } else {
                    top = mid - 1;
                }
            }

            Logger.debug("Result of testing max. client identifier length: {} bytes", mid);
            Logger.trace("Setting max. client identifier length to {} bytes for further tests", mid);
            maxClientIdLength = mid;
            return new ClientIdLengthTestResults(mid, connectResults);
        }
    }

    private boolean testClientIdLength(
            final @NotNull List<Tuple<Integer, String>> connectResults, final int clientIdLength) {
        Logger.debug("Testing client identifier with a length of {} bytes", clientIdLength);

        final String currentIdentifier = Strings.repeat(ONE_BYTE, clientIdLength);
        final Mqtt5Client currClient = getClientBuilder().identifier(currentIdentifier).build();

        try {
            final Mqtt5ConnAck connAck = currClient.toBlocking().connect();
            connectResults.add(Tuple.of(clientIdLength, connAck.getReasonCode().toString()));
            if (connAck.getReasonCode() != Mqtt5ConnAckReasonCode.SUCCESS) {
                Logger.debug("Received non-successful reason code {}", connAck.getReasonCode());
                return false;
            }
        } catch (final Mqtt5ConnAckException connAckEx) {
            connectResults.add(Tuple.of(clientIdLength, connAckEx.getMqttMessage().getReasonCode().toString()));
            disconnectIfConnected(currClient);
            return false;
        } catch (final Exception ex) {
            Logger.error(ex,
                    "Connect with client id length {} bytes",
                    currClient.getConfig().getClientIdentifier().map(id -> id.toString().getBytes().length).orElse(0));
            connectResults.add(Tuple.of(clientIdLength, "UNDEFINED_FAILURE"));
            disconnectIfConnected(currClient);
            return false;
        }

        disconnectIfConnected(currClient);

        return true;

    }

    public @NotNull AsciiCharsInClientIdTestResults testAsciiCharsInClientId() {
        Logger.debug("Testing ascii characters in client identifier");

        final String ASCII = " !\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
        final List<Tuple<Character, String>> connectResults = new LinkedList<>();

        boolean allSuccess = false;
        final Mqtt5Client client = getClientBuilder().identifier(ASCII).build();

        if (ASCII.length() <= maxClientIdLength) {
            try {
                Logger.trace("Testing client identifier '{}'", ASCII);
                client.toBlocking().connect();
                allSuccess = true;
            } catch (final Exception e) {
                Logger.error(e, "Could not connect with Client ID '" + ASCII + "'");
            }

            disconnectIfConnected(client);
        }

        if (allSuccess) {
            Logger.trace("Result of testing ascii characters: All supported");
        } else {
            for (int i = 0; i < ASCII.length(); i++) {
                testAsciiChar(connectResults, ASCII.charAt(i));
            }
            Logger.debug("Result of testing ascii character in client identifier: Unsupported characters {}",
                    connectResults.toString());
        }
        return new AsciiCharsInClientIdTestResults(connectResults);
    }

    private void testAsciiChar(final @NotNull List<Tuple<Character, String>> connectResults, final char asciiChar) {
        Logger.debug("Testing ascii character '{}'", asciiChar);
        final Mqtt5Client client = getClientBuilder().identifier(String.valueOf(asciiChar)).build();

        try {
            client.toBlocking().connect();
        } catch (final Mqtt5ConnAckException ex) {
            Logger.debug(ex, "Could not connect client identifier with ascii char '{}'", asciiChar);
            connectResults.add(Tuple.of(asciiChar, ex.getMqttMessage().getReasonCode().toString()));
        } catch (final Exception ex) {
            Logger.error("Connect with Ascii char '{}' failed", asciiChar);
            connectResults.add(Tuple.of(asciiChar, null));
        }

        disconnectIfConnected(client);
    }

    // Helpers
    public void setMaxTopicLength(final int maxTopicLength) {
        this.maxTopicLength = maxTopicLength;
    }

    public void setMaxQos(final @NotNull MqttQos qos) {
        maxQos = qos;
    }

    private @NotNull Mqtt5Client buildClient() {
        return getClientBuilder().build();
    }

    private @NotNull Mqtt5ClientBuilder getClientBuilder() {
        return Mqtt5Client.builder().serverHost(host).serverPort(port).simpleAuth(buildAuth()).sslConfig(sslConfig);
    }

    private @Nullable Mqtt5SimpleAuth buildAuth() {
        if (username != null && password != null) {
            return Mqtt5SimpleAuth.builder().username(username).password(password).build();
        } else if (username != null) {
            return Mqtt5SimpleAuth.builder().username(username).build();
        } else if (password != null) {
            return Mqtt5SimpleAuth.builder().password(password).build();
        } else {
            return null;
        }
    }

    private void disconnectIfConnected(final @NotNull Mqtt5Client... clients) {
        for (final Mqtt5Client client : clients) {
            if (client.getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }
}
