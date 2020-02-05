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

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3DisconnectException;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Mqtt3TestClient {

    final static String TOPIC_UUID_1 = "e67997ee482611eab77f2e728ce88125";
    final static String TOPIC_UUID_2 = "df42d218482811eab77f2e728ce88125";
    final Mqtt3BlockingClient client;

    public Mqtt3TestClient(final @NotNull String host, final @NotNull Integer port) {
        client = Mqtt3Client.builder()
                .serverHost(host)
                .serverPort(port)
                .buildBlocking();
    }

    public @Nullable Mqtt3ConnAck connect() {
        try {
            return client.connect();
        }
        catch (final Mqtt3DisconnectException ex) {
            return null;
        }
    }

    public @Nullable Mqtt3SubAck testWildcardSubscription() {
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

        final Mqtt3SubAck subAck = client.toAsync().subscribe(mqttSubscribe, publish -> {
            if (Arrays.equals(testPlus, publish.getPayloadAsBytes())) { countDownLatch.countDown(); }
            else if (Arrays.equals(testHash, publish.getPayloadAsBytes())) { countDownLatch.countDown(); }
        }).join();

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

        client.unsubscribeWith()
                .addTopicFilter(TOPIC_UUID_1 + "/+")
                .addTopicFilter(TOPIC_UUID_2 + "/#")
                .send();

        if (countDownLatch.getCount() > 0) { return null; }
        else { return subAck; }
    }

    public int testTopicLength() {
        //TODO
        final StringBuilder topicName = new StringBuilder();
        // A 4 byte Unicode character
        final char[] chars = Character.toChars(0x1F701);
        final String fourBytes = new String(chars);
        topicName.append(fourBytes);
        int bytes = 4;

        while (bytes < 65535) {
            final Mqtt3SubAck subAck = client.subscribeWith()
                    .topicFilter(topicName.toString())
                    .qos(MqttQos.AT_MOST_ONCE)
                    .send();

            if (subAck.getReturnCodes().contains(Mqtt3SubAckReturnCode.FAILURE)) {
                break;
            }

            topicName.append(fourBytes);
            bytes += 4;
        }
        return bytes;
    }



}
