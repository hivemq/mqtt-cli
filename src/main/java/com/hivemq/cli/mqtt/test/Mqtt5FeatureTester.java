package com.hivemq.cli.mqtt.test;

import com.google.common.base.Strings;
import com.hivemq.cli.mqtt.test.results.*;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Mqtt5FeatureTester {

    private int maxTopicLength = -1;
    private final String host;
    private final int port;
    private final String username;
    private final ByteBuffer password;
    private final MqttClientSslConfig sslConfig;
    private final int timeOut;

    public Mqtt5FeatureTester(final @NotNull String host,
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

    // Tests

    public @Nullable Mqtt5ConnAck testConnect() {
        final Mqtt5Client mqtt5Client = buildClient();

        try {
            return mqtt5Client.toBlocking().connect();
        }
        catch (final Mqtt5ConnAckException connAckEx) { return connAckEx.getMqttMessage(); }
        catch (final Exception ex) {
            Logger.error(ex, "Could not connect MQTT5 client");
            return null;
        }
        finally {
            disconnectIfConnected(mqtt5Client);
        }
    }

    public @NotNull QosTestResult testQos(final @NotNull MqttQos qos, final int tries) {
        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
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
        }
        catch (final Exception ex) {
            Logger.error(ex, "Could not subscribe with QoS {}", qos.ordinal());
        }

        final long before = System.nanoTime();

        for (int i = 0; i < tries; i++) {
            try {
                publisher.toAsync().publishWith()
                        .topic(topic)
                        .qos(qos)
                        .payload(payload)
                        .send();
            }
            catch (final Exception ex) {
                Logger.error("Could not publish with QoS {}", qos.ordinal());
            }
        }

        try { countDownLatch.await(timeOut, TimeUnit.SECONDS); }
        catch (InterruptedException e) {
            Logger.error("Interrupted while waiting for QoS {} publish to arrive at subscriber", qos.ordinal());
        }

        final long after = System.nanoTime();
        final long timeToComplete = after - before;

        disconnectIfConnected(publisher, subscriber);

        return new QosTestResult(totalReceived.get(), timeToComplete);
    }

    public @NotNull TestResult testRetain() {
        final Mqtt5Client publisher = buildClient();
        final Mqtt5Client subscriber = buildClient();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
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
        catch (final Exception ex) {
            if (!(ex instanceof Mqtt5PubAckException)) {
                Logger.error(ex, "Retained publish failed");
            }
            return TestResult.PUBLISH_FAILED;
        }

        subscriber.toBlocking().connect();

        try {
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
        }
        catch (final Exception ex) {
            if (!(ex instanceof Mqtt5SubAckException)) {
                Logger.error(ex, "Retained subscribe failed");
            }
            return TestResult.SUBSCRIBE_FAILED;
        }

        try { countDownLatch.await(timeOut, TimeUnit.SECONDS); }
        catch (final InterruptedException ex) {
            Logger.error(ex, "Interrupted while waiting for retained publish to arrive at subscriber");
        }

        disconnectIfConnected(publisher, subscriber);

        return countDownLatch.getCount() == 0 ? TestResult.OK : TestResult.TIME_OUT;
    }

    public @NotNull WildcardSubscriptionsTestResult testWildcardSubscriptions() {
        final TestResult plusWildcardResult = testWildcard("+", "test");
        final TestResult hashWildcardResult = testWildcard("#", "test/subtopic");

        return new WildcardSubscriptionsTestResult(plusWildcardResult, hashWildcardResult);
    }

    private @NotNull TestResult testWildcard(final String subscribeWildcardTopic, final String publishTopic) {
        final Mqtt5Client subscriber = buildClient();
        final Mqtt5Client publisher = buildClient();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final String subscribeToTopic = topic + "/" + subscribeWildcardTopic;
        final String publishToTopic = topic + "/" + publishTopic;
        final byte[] payload = "WILDCARD_TEST".getBytes();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Consumer<Mqtt5Publish> publishCallback = publish -> {
            if (Arrays.equals(publish.getPayloadAsBytes(), payload)) { countDownLatch.countDown(); }
        };

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        try {
            subscriber.toAsync().subscribeWith()
                    .topicFilter(subscribeToTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback(publishCallback)
                    .send()
                    .join();
        }
        catch (final Exception ex) {
            if (!(ex instanceof Mqtt5SubAckException)) {
                Logger.error(ex, "Subscribe to wildcard topic '{}' failed", subscribeToTopic);
            }
            return TestResult.SUBSCRIBE_FAILED;
        }

        try {
            publisher.toBlocking().publishWith()
                    .topic(publishToTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(payload)
                    .send();
        }
        catch (final Exception ex) {
            if (!(ex instanceof Mqtt5PubAckException)) {
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

    public @NotNull PayloadTestResults testPayloadSize(final int maxSize) {
        final Mqtt5Client subscriber = buildClient();
        final Mqtt5Client publisher = buildClient();
        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();
        final String topic = (maxTopicLength == -1 ? TopicUtils.generateTopicUUID() : TopicUtils.generateTopicUUID(maxTopicLength));
        final Mqtt5BlockingClient.Mqtt5Publishes publishes = subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);
        final String oneByte = "a";
        int top = maxSize;
        int bottom = 0;
        int mid = -1;

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        subscriber.toBlocking().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send();

        // Binary search the payload size
        while (bottom <= top) {
            mid = (bottom + top) / 2;
            final String currentPayload = Strings.repeat(oneByte, mid);
            final Mqtt5Publish publish = Mqtt5Publish.builder()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(currentPayload.getBytes())
                    .build();

            try {
                publisher.toBlocking().publish(publish);
            }
            catch (final Exception ex) {
                if (!(ex instanceof  Mqtt5PubAckException)) {
                    Logger.error(ex, "Publish with payload of size {} bytes failed", currentPayload.getBytes().length);
                }
                testResults.add(new Tuple<>(mid, TestResult.PUBLISH_FAILED));
                top = mid -1;
                continue;
            }

            try {
                final Optional<Mqtt5Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
                if (!receive.isPresent()) {
                    testResults.add(new Tuple<>(mid, TestResult.TIME_OUT));
                    top = mid - 1;
                    continue;
                }
                else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentPayload.getBytes())) {
                    testResults.add(new Tuple<>(mid, TestResult.WRONG_PAYLOAD));
                    top = mid - 1;
                    continue;
                }

            } catch (InterruptedException e) {
                Logger.error(e, "Interrupted while waiting for subscriber to receive payload with length {} bytes", currentPayload.getBytes().length);
            }

            testResults.add(new Tuple<>(mid, TestResult.OK));
            bottom = mid + 1;
        }

        disconnectIfConnected(publisher, subscriber);

        return new PayloadTestResults(mid, testResults);
    }


    public @NotNull TopicLengthTestResults testTopicLength() {
        final Mqtt5Client subscriber = buildClient();
        final Mqtt5Client publisher = buildClient();
        final Mqtt5BlockingClient.Mqtt5Publishes publishes = subscriber.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);
        final List<Tuple<Integer, TestResult>> testResults = new LinkedList<>();
        final String oneByte = "a";
        int top = 65535;
        int bottom = 0;
        int mid = -1;

        subscriber.toBlocking().connect();
        publisher.toBlocking().connect();

        // Binary search the right topic length
        while (bottom <= top) {
            mid = (bottom + top) / 2;
            final String currentTopicName = Strings.repeat(oneByte, mid);
            final Mqtt5Publish publish = Mqtt5Publish.builder()
                    .topic(currentTopicName)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(currentTopicName.getBytes())
                    .build();
            final Mqtt5Subscribe subscribe = Mqtt5Subscribe.builder()
                    .topicFilter(currentTopicName)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .build();

            // Test subscribe to topic
            try { subscriber.toBlocking().subscribe(subscribe); }
            catch (final Exception ex) {
                if (!(ex instanceof Mqtt5SubAckException)) {
                    Logger.error(ex, "Subscribe to topic of length {} bytes failed", currentTopicName.getBytes().length);
                }
                testResults.add(new Tuple<>(mid, TestResult.SUBSCRIBE_FAILED));
                top = mid - 1;
                continue;
            }

            // Test publish to topic
            try {
                publisher.toBlocking().publish(publish);
            }
            catch (final Exception ex) {
                if (!(ex instanceof  Mqtt5PubAckException)) {
                    Logger.error(ex, "Publish to topic of length {}", currentTopicName.getBytes().length);
                }
                testResults.add(new Tuple<>(mid, TestResult.PUBLISH_FAILED));
                top = mid - 1;
                continue;
            }

            // Subscriber retrieves payload
            try {
                final Optional<Mqtt5Publish> receive = publishes.receive(timeOut, TimeUnit.SECONDS);
                if (!receive.isPresent()) {
                    testResults.add(new Tuple<>(mid, TestResult.TIME_OUT));
                    top = mid - 1;
                    continue;
                }
                else if (!Arrays.equals(receive.get().getPayloadAsBytes(), currentTopicName.getBytes())) {
                    testResults.add(new Tuple<>(mid, TestResult.WRONG_PAYLOAD));
                    top = mid - 1;
                    continue;
                }
            }
            catch (InterruptedException e) {
                Logger.error(e, "Interrupted while waiting to receive publish to topic with {} bytes", currentTopicName.getBytes().length);
            }

            // Everything successful
            testResults.add(new Tuple<>(mid, TestResult.OK));
            bottom = mid + 1;
        }

        disconnectIfConnected(publisher, subscriber);

        return new TopicLengthTestResults(mid, testResults);
    }

    public @NotNull ClientIdLengthTestResults testClientIdLength() {
        final List<Tuple<Integer, String>> connectResults = new LinkedList<>();
        final String oneByte = "a";
        int top = 65535;
        int bottom = 0;
        int mid = -1;

        // Binary search the right client id length
        while (bottom <= top) {
            mid = (bottom + top) / 2;
            final String currentIdentifier = Strings.repeat(oneByte, mid);
            final Mqtt5Client currClient = getClientBuilder()
                    .identifier(currentIdentifier)
                    .build();

            try {
                final Mqtt5ConnAck connAck = currClient.toBlocking().connect();
                connectResults.add(new Tuple<>(mid, connAck.getReasonCode().toString()));
                if (connAck.getReasonCode() != Mqtt5ConnAckReasonCode.SUCCESS) {
                    top = mid - 1;
                    continue;
                }
            }
            catch (final Mqtt5ConnAckException connAckEx) {
                connectResults.add(new Tuple<>(mid, connAckEx.getMqttMessage().getReasonCode().toString()));
                top = mid - 1;
                continue;
            }
            catch (final Exception ex) {
                if (!(ex instanceof  Mqtt5ConnAckException)) {
                    Logger.error(ex, "Connect with client id length {} bytes",
                            currClient.getConfig().getClientIdentifier()
                                    .map(id -> id.toString().getBytes().length).orElse(0));
                }
                connectResults.add(new Tuple<>(mid, null));
                top = mid -1;
                continue;
            }

            bottom = mid + 1;
            disconnectIfConnected(currClient);
        }

        return new ClientIdLengthTestResults(mid, connectResults);
    }

    public @NotNull AsciiCharsInClientIdTestResults testAsciiCharsInClientId() {
        final String ASCII = " !\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
        final List<Tuple<Character, String>> connectResults = new LinkedList<>();

        for (int i = 0; i < ASCII.length(); i++) {
            final String currChar = String.valueOf(ASCII.charAt(i));
            final Mqtt5Client client = getClientBuilder()
                    .identifier(currChar)
                    .build();

            try { client.toBlocking().connect(); }
            catch (final Mqtt5ConnAckException ex) {
                connectResults.add(new Tuple<>(currChar.charAt(0), ex.getMqttMessage().getReasonCode().toString()));
            }
            catch (final Exception ex) {
                Logger.error("Connect with Ascii char '{}' failed", currChar);
                connectResults.add(new Tuple<>(currChar.charAt(0), null));
            }

            disconnectIfConnected(client);
        }

        return new AsciiCharsInClientIdTestResults(connectResults);
    }

    // Helpers


    public void setMaxTopicLength(final int maxTopicLength) {
        this.maxTopicLength = maxTopicLength;
    }

    private @NotNull Mqtt5Client buildClient() {
        return getClientBuilder()
                .build();
    }

    private @NotNull Mqtt5ClientBuilder getClientBuilder() {
        final Mqtt5ClientBuilder mqtt5ClientBuilder = Mqtt5Client.builder()
                .serverHost(host)
                .serverPort(port)
                .simpleAuth(buildAuth());

        if (sslConfig != null) { mqtt5ClientBuilder.sslConfig(sslConfig); }

        return mqtt5ClientBuilder;
    }

    private @Nullable Mqtt5SimpleAuth buildAuth() {
        if (username != null && password != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(username)
                    .password(password)
                    .build();
        }
        else if (username != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(username)
                    .build();
        }
        else if (password != null) {
            return Mqtt5SimpleAuth.builder()
                    .password(password)
                    .build();
        }
        else {
            return null;
        }
    }

    private void disconnectIfConnected(final @NotNull Mqtt5Client ... clients) {
        for (Mqtt5Client client: clients) {
            if (client.getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }
}
