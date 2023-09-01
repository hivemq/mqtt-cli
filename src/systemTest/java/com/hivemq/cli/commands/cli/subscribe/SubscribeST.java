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

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.subscribe.RetainHandling;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.extensions.packets.subscribe.SubscriptionImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulConnectAndSubscribe(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        publishMessage("topic", "message");

        executionResult.awaitStdOut("message");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_qos(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-q");
        subscribeCommand.add("1");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.AT_LEAST_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopics(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.remove("-t");
        subscribeCommand.remove("topic");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic1");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic2");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic3");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand)
                .awaitStdOut("received SUBACK")
                .awaitStdOut("received SUBACK")
                .awaitStdOut("received SUBACK");

        publishMessage("topic1", "message1");
        executionResult.awaitStdOut("message1");

        publishMessage("topic2", "message2");
        executionResult.awaitStdOut("message2");

        publishMessage("topic3", "message3");
        executionResult.awaitStdOut("message3");


        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic1", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(1), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic2", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(2), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic3", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopicsMultipleQoS(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.remove("-t");
        subscribeCommand.remove("topic");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic1");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic2");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic3");
        subscribeCommand.add("-q");
        subscribeCommand.add("0");
        subscribeCommand.add("-q");
        subscribeCommand.add("1");
        subscribeCommand.add("-q");
        subscribeCommand.add("2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand)
                .awaitStdOut("received SUBACK")
                .awaitStdOut("received SUBACK")
                .awaitStdOut("received SUBACK");

        publishMessage("topic1", "message1");
        executionResult.awaitStdOut("message1");

        publishMessage("topic2", "message2");
        executionResult.awaitStdOut("message2");

        publishMessage("topic3", "message3");
        executionResult.awaitStdOut("message3");


        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic1", Qos.AT_MOST_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(1), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic2", Qos.AT_LEAST_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(2), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic3", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_outputFile(final char mqttVersion) throws Exception {
        final Path messageFile = Files.createTempFile("message-file", ".txt");
        messageFile.toFile().deleteOnExit();

        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-of");
        subscribeCommand.add(messageFile.toString());

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        publishMessage("topic", "message");

        executionResult.awaitStdOut("message");

        final List<String> readLines = Files.readAllLines(messageFile);
        assertEquals(1, readLines.size());
        assertEquals("message", readLines.get(0));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-up");
        subscribeCommand.add("key1=value1");
        subscribeCommand.add("-up");
        subscribeCommand.add("key2=value2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Subscribe user properties were set but are unused in MQTT version MQTT_3_1_1");
        }

        publishMessage("topic", "message");

        executionResult.awaitStdOut("message");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));

            subscribeAssertion.setSubscriptions(expectedSubscriptions);
            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties = UserPropertiesImpl.of(ImmutableList.of(
                        MqttUserProperty.of("key1", "value1"),
                        MqttUserProperty.of("key2", "value2")));
                subscribeAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_base64(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-b64");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        publishMessage("topic", "message");

        final String encodedPayload = Base64.getEncoder().encodeToString("message".getBytes(StandardCharsets.UTF_8));
        executionResult.awaitStdOut(encodedPayload);

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showTopic(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-T");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        publishMessage("topic", "message");

        executionResult.awaitStdOut("topic: message");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showJson(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-J");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribe(executionResult);

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("property1", "value1");
        jsonObject.addProperty("property2", "value2");
        jsonObject.addProperty("property3", "value3");
        publishMessage("topic", jsonObject.toString());

        executionResult.awaitStdOut(
                "{\n" + "  \"topic\": \"topic\",\n" + "  \"payload\": {\n" + "    \"property1\": \"value1\",\n" +
                        "    \"property2\": \"value2\",\n" + "    \"property3\": \"value3\"\n" + "  },\n");
        executionResult.awaitStdOut("\"qos\": \"EXACTLY_ONCE\",");
        executionResult.awaitStdOut("\"receivedAt\":");
        executionResult.awaitStdOut("\"retain\": false");
        executionResult.awaitStdOut("}");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions = List.of(createSubscription("topic", Qos.EXACTLY_ONCE));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribeMissingTopic() throws Exception {
        final List<String> subscribeCommand =
                List.of("sub", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()));

        final ExecutionResult executionResult = MqttCli.execute(subscribeCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Missing required option: '--topic=<topics>'"));
    }

    private @NotNull List<String> defaultSubscribeCommand(final char mqttVersion) {
        final ArrayList<String> subscribeCommand = new ArrayList<>();
        subscribeCommand.add("sub");
        subscribeCommand.add("-h");
        subscribeCommand.add(hivemq.getHost());
        subscribeCommand.add("-p");
        subscribeCommand.add(String.valueOf(hivemq.getMqttPort()));
        subscribeCommand.add("-V");
        subscribeCommand.add(String.valueOf(mqttVersion));
        subscribeCommand.add("-i");
        subscribeCommand.add("cliTest");
        subscribeCommand.add("-t");
        subscribeCommand.add("topic");
        subscribeCommand.add("-d");
        return subscribeCommand;
    }

    private void assertSubscribe(final @NotNull ExecutionResultAsync executionResultAsync) {
        executionResultAsync.awaitStdOut("sending CONNECT");
        executionResultAsync.awaitStdOut("received CONNACK");
        executionResultAsync.awaitStdOut("sending SUBSCRIBE");
        executionResultAsync.awaitStdOut("received SUBACK");
    }

    private void publishMessage(final @NotNull String topic, final @NotNull String message) {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }

    private Subscription createSubscription(final @NotNull String topic, final @NotNull Qos qos) {
        return new SubscriptionImpl(topic, qos, RetainHandling.SEND, false, false);
    }
}
