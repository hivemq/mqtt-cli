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
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3PubAckException;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient.*;

public class Mqtt3FeatureTester {

    private int maxTopicLength = -1;
    private final String host;
    private final int port;
    private final String username;
    private final ByteBuffer password;

    public Mqtt3FeatureTester(final @NotNull String host,
                              final @NotNull Integer port,
                              final @Nullable String username,
                              final @Nullable ByteBuffer password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // Test methods

    public @Nullable Mqtt3ConnAck testConnect() {
        final Mqtt3Client client = buildClient();

        try { return client.toBlocking().connect(); }
        catch (final Mqtt3ConnAckException ex) { return ex.getMqttMessage(); }
        catch (final ConnectionFailedException | Mqtt3DisconnectException ex) { return null; }
        finally {
            if (client.getConfig().getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }

    public boolean testWildcardSubscriptions() {
        final Mqtt3Client client = buildClient();
        final String topic1 = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final String topic2 = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final byte[] testPlus = "WILDCARD_TEST_PLUS".getBytes();
        final byte[] testHash = "WILDCARD_TEST_HASH".getBytes();
        final String plusTopic =  topic1 + "/+";
        final String hashTopic = topic2 + "/#";
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        client.toBlocking().connect();

        final Mqtt3Subscribe mqttSubscribe = Mqtt3Subscribe.builder()
                .topicFilter(plusTopic).qos(MqttQos.EXACTLY_ONCE)
                .addSubscription()
                .topicFilter(hashTopic).qos(MqttQos.EXACTLY_ONCE)
                .applySubscription()
                .build();

        try { client.toBlocking().subscribe(mqttSubscribe); }
        catch (final Exception ex) { return false; }

        final Mqtt3Publishes publishes = client.toBlocking().publishes(MqttGlobalPublishFilter.SUBSCRIBED);

        CompletableFuture.runAsync(() -> {

                while (countDownLatch.getCount() > 0) {
                    try {
                    publishes.receive(10, TimeUnit.SECONDS).ifPresent(mqtt3Publish -> {
                        if (Arrays.equals(testPlus, mqtt3Publish.getPayloadAsBytes())) { countDownLatch.countDown();}
                        else if (Arrays.equals(testHash, mqtt3Publish.getPayloadAsBytes())) { countDownLatch.countDown();}
                    });
                    } catch (InterruptedException e) { e.printStackTrace(); }
                }
        });

        client.toBlocking().publishWith()
                .topic(topic1 + "/a")
                .payload(testPlus)
                .send();

        client.toBlocking().publishWith()
                .topic(topic2 + "/a/b")
                .payload(testHash)
                .send();

        try { countDownLatch.await(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return countDownLatch.getCount() <= 0;
    }

    public boolean testRetain() {
        final Mqtt3Client client = buildClient();
        final String topic = (maxTopicLength == -1 ? generateTopicUUID() : generateTopicUUID(maxTopicLength));
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.toBlocking().connect();

        final Mqtt3Publish publish = Mqtt3Publish.builder()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("TEST_RETAIN".getBytes())
                .retain(true)
                .build();

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .build();

        try { client.toBlocking().publish(publish); }
        catch (final Exception ex) { return false; }


        try {
            client.toAsync().subscribe(subscribe, mqtt3Publish -> {
                if (Arrays.equals(mqtt3Publish.getPayloadAsBytes(), "TEST_RETAIN".getBytes())) {
                    if(mqtt3Publish.isRetain()) {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        catch (final Exception ex) { return false; }

        try { countDownLatch.await(10, TimeUnit.SECONDS); }
        catch (final InterruptedException ex) { ex.printStackTrace(); }

        client.toBlocking().publishWith()
                .topic(topic)
                .retain(true)
                .qos(MqttQos.EXACTLY_ONCE)
                .send();

        return countDownLatch.getCount() == 0;
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
                .qos(MqttQos.EXACTLY_ONCE)
                .send();

        // Binary search the payload size
        while (bottom.get() <= top.get()) {
            final String currentPayload = Strings.repeat(oneByte, mid);
            final Mqtt3Publish publish = Mqtt3Publish.builder()
                    .topic(topic)
                    .qos(MqttQos.EXACTLY_ONCE)
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
            final Mqtt3Client currClient = mqtt3ClientBuilder.identifier(currentIdentifier).build();

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
    
    public @NotNull String testClientIdAsciiChars() {
        final String ASCII = " !\"#$%&\\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        final StringBuilder unsupportedChars = new StringBuilder();

        for (int i = 0; i < ASCII.length(); i++) {
            final String currChar = String.valueOf(ASCII.charAt(i));
            final Mqtt3Client client = getClientBuilder()
                    .identifier(currChar)
                    .build();

            try { client.toBlocking().connect(); }
            catch (final Mqtt3ConnAckException connAckEx) { unsupportedChars.append(currChar); }

            if (client.getConfig().getState().isConnected()) { client.toBlocking().disconnect(); }
        }

        return unsupportedChars.toString();
    }

    // Getter / Setter

    public void setMaxTopicLength(final int topicLength) {
        maxTopicLength = topicLength;
    }

    // Helpers

    private @NotNull Mqtt3Client buildClient() {
        return getClientBuilder()
                .build();
    }

    private @NotNull Mqtt3ClientBuilder getClientBuilder() {
        return Mqtt3Client.builder()
                .serverHost(host)
                .serverPort(port);
    }

    private @NotNull String generateTopicUUID() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.replace("-","");
    }

    private @NotNull String generateTopicUUID(final int maxLength) {
        return generateTopicUUID().substring(0, maxLength);
    }

}
