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

package com.hivemq.cli.commands.cli.subscribe;

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.subscribe.RetainHandling;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import com.hivemq.extensions.packets.subscribe.SubscriptionImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;

public class SubscribeJsonST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulConnectAndSubscribe(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "testTopic",
                "-d",
                "-J");

        final ExecutionResultAsync executionResultAsync = mqttCli.executeAsync(subscribeCommand);
        executionResultAsync.awaitStdOut("sending CONNECT")
                .awaitStdOut("received CONNACK")
                .awaitStdOut("sending SUBSCRIBE")
                .awaitStdOut("received SUBACK");

        publishMessage();

        if (mqttVersion == '5') {
            executionResultAsync.awaitStdOut("\"topic\": \"testTopic\"")
                    .awaitStdOut("\"payload\": \"testPayload\"")
                    .awaitStdOut("\"qos\": \"EXACTLY_ONCE\"")
                    .awaitStdOut("\"receivedAt\": ")
                    .awaitStdOut("\"retain\": false")
                    .awaitStdOut("\"contentType\": \"testContentType\"")
                    .awaitStdOut("\"payloadFormatIndicator\": \"UTF_8\"")
                    .awaitStdOut("\"messageExpiryInterval\": 1337")
                    .awaitStdOut("\"responseTopic\": \"testResponseTopic\"")
                    .awaitStdOut("\"correlationData\": \"testCorrelationData\"")
                    .awaitStdOut("\"userProperties\": ")
                    .awaitStdOut("\"name\": \"testProperty1\"")
                    .awaitStdOut("\"value\": \"testPropertyValue1\"")
                    .awaitStdOut("\"name\": \"testProperty1\"")
                    .awaitStdOut("\"value\": \"testPropertyValue1Duplicate\"")
                    .awaitStdOut("\"name\": \"testProperty2\"")
                    .awaitStdOut("\"value\": \"testPropertyValue2\"");
        } else {
            executionResultAsync.awaitStdOut("\"topic\": \"testTopic\"")
                    .awaitStdOut("\"payload\": \"testPayload\"")
                    .awaitStdOut("\"qos\": \"EXACTLY_ONCE\"")
                    .awaitStdOut("\"receivedAt\": ")
                    .awaitStdOut("\"retain\": false");
        }

        assertConnectPacket(hivemq.getConnectPackets().getFirst(),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("testTopic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    private void publishMessage() {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("testTopic")
                .qos(MqttQos.EXACTLY_ONCE)
                .contentType("testContentType")
                .correlationData("testCorrelationData".getBytes(StandardCharsets.UTF_8))
                .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                .messageExpiryInterval(1337)
                .responseTopic("testResponseTopic")
                .payload("testPayload".getBytes(StandardCharsets.UTF_8))
                .userProperties()
                .add("testProperty1", "testPropertyValue1")
                .add("testProperty1", "testPropertyValue1Duplicate")
                .add("testProperty2", "testPropertyValue2")
                .applyUserProperties()
                .retain(false)
                .send();
    }
}
