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
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient.*;

public class Mqtt3TestClient {

    private final static String TOPIC_UUID_1 = "e67997ee482611eab77f2e728ce88125";
    private final static String TOPIC_UUID_2 = "df42d218482811eab77f2e728ce88125";
    private final Mqtt3BlockingClient client;

    public Mqtt3TestClient(final @NotNull String host, final @NotNull Integer port) {
        client = Mqtt3Client.builder()
                .serverHost(host)
                .serverPort(port)
                .buildBlocking();
    }

    public @Nullable Mqtt3ConnAck testConnect() {
        try { return client.connect(); }
        catch (final Mqtt3DisconnectException ex) { return null; }
    }

    private boolean reconnect() {
        if (client.getConfig().getState() == MqttClientState.CONNECTED) {
            client.disconnect();
        }
        Mqtt3ConnAck connAck = client.connect();
        return !connAck.getReturnCode().isError();
    }

    public boolean testWildcardSubscription() {
        reconnect();
        final byte[] testPlus = "WILDCARD_TEST_PLUS".getBytes();
        final byte[] testHash = "WILDCARD_TEST_HASH".getBytes();
        final String plusTopic = TOPIC_UUID_1 + "/+";
        final String hashTopic = TOPIC_UUID_2 + "/#";

        final Mqtt3Subscribe mqttSubscribe = Mqtt3Subscribe.builder()
                .topicFilter(plusTopic).qos(MqttQos.EXACTLY_ONCE)
                .addSubscription()
                .topicFilter(hashTopic).qos(MqttQos.EXACTLY_ONCE)
                .applySubscription()
                .build();

        final CountDownLatch countDownLatch = new CountDownLatch(2);

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

        client.publishWith()
                .topic(TOPIC_UUID_1 + "/a")
                .payload(testPlus)
                .send();

        client.publishWith()
                .topic(TOPIC_UUID_2 + "/a/b")
                .payload(testHash)
                .send();

        try { countDownLatch.await(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return countDownLatch.getCount() <= 0;
    }

    public boolean testRetain() {
        reconnect();
        final Mqtt3Publish publish = Mqtt3Publish.builder()
                .topic(TOPIC_UUID_1)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("TEST_RETAIN".getBytes())
                .retain(true)
                .build();

        final Mqtt3Subscribe subscribe = Mqtt3Subscribe.builder()
                .topicFilter(TOPIC_UUID_1)
                .qos(MqttQos.EXACTLY_ONCE)
                .build();

        try { client.publish(publish); }
        catch (final Exception ex) { return false; }

        final CountDownLatch countDownLatch = new CountDownLatch(1);

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

        return countDownLatch.getCount() == 0;
    }

    public int testTopicLength() {
        reconnect();
        final String oneByte = "a";
        int top = 65535;
        int bottom = 0;
        int mid = top / 2;

        while (bottom <= top) {
            String currentTopicName = Strings.repeat(oneByte, mid);

            try {
                Mqtt3Publish publish = Mqtt3Publish.builder()
                        .topic(currentTopicName)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .build();

                client.toAsync().publish(publish)
                        .get(10, TimeUnit.SECONDS);

                bottom = mid + 1;
            }
            catch (final Exception ex) {
                top = mid - 1;
            }
            mid = (bottom + top) / 2;
        }
        return mid;
    }

    public int testClientIdLength() {
        // TODO
        String oneByte = "";
        final Mqtt3ClientBuilder mqtt3ClientBuilder = Mqtt3Client.builder()
                .serverHost(client.getConfig().getServerHost())
                .serverPort(client.getConfig().getServerPort());
        int top = 65535;
        int bottom = 0;
        int mid = top / 2;


        while (bottom <= top) {
            String currentIdentifier = Strings.repeat(oneByte, mid);

            try {
                Mqtt3ConnAck connAck = mqtt3ClientBuilder.identifier(currentIdentifier).build().toBlocking().connect();
                if (!connAck.getReturnCode().isError()) {
                    bottom = mid + 1;
                }
                else {
                    top = mid - 1;
                }
            }
            catch (final Exception ex) {
                top = mid - 1;
            }
            mid = (bottom + top) / 2;
        }
        return mid;
    }


}
