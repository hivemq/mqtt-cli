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

package com.hivemq.cli.commands.shell;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellSubscribeST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulSubscribe(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopics(final char mqttVersion) throws Exception {
        //FIXME Subscribe command should subscribe to all topics in one packet and not send separate subscribes
        final List<String> subscribeCommand = List.of("sub", "-t", "test1", "-t", "test2", "-t", "test3");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK")
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK")
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test1", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(1), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test2", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(2), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test3", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopicsMultipleQos(final char mqttVersion) throws Exception {
        //FIXME Subscribe command should subscribe to all topics in one packet and not send separate subscribes
        final List<String> subscribeCommand =
                List.of("sub", "-t", "test1", "-t", "test2", "-t", "test3", "-q", "0", "-q", "1", "-q", "2");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK")
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK")
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test1", Qos.AT_MOST_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(1), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test2", Qos.AT_LEAST_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        assertSubscribePacket(hivemq.getSubscribePackets().get(2), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("test3", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-up", "key1=value1", "-up", "key2=value2");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Subscribe user properties were set but are unused in MQTT version MQTT_3_1_1");
            awaitOutput.awaitLog("Subscribe user properties were set but are unused in MQTT version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending SUBSCRIBE");
        awaitOutput.awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                    Qos.EXACTLY_ONCE,
                    RetainHandling.SEND,
                    false,
                    false)));

            if (mqttVersion == '5') {
                final UserPropertiesImpl userProperties =
                        UserPropertiesImpl.of(ImmutableList.of(MqttUserProperty.of("key1", "value1"),
                                MqttUserProperty.of("key2", "value2")));
                subscribeAssertion.setUserProperties(userProperties);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_stay(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-s");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith().topic("test").payload("message".getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitStdOut("message");
        awaitOutput.awaitLog("received PUBLISH ('message')");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_outputToConsole(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-oc");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith().topic("test").payload("message".getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitStdOut("message");
        awaitOutput.awaitLog("received PUBLISH ('message')");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_outputToFile(final char mqttVersion) throws Exception {
        final Path outputFile = Files.createTempFile("publishes", ".txt");
        outputFile.toFile().deleteOnExit();
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-of", outputFile.toString());
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith().topic("test").payload("message".getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitLog("received PUBLISH ('message')");

        final List<String> readLines = Files.readAllLines(outputFile);
        assertEquals(1, readLines.size());
        assertEquals("message", readLines.get(0));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_base64(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-s", "-b64");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith().topic("test").payload("message".getBytes(StandardCharsets.UTF_8)).send();

        final String encodedPayload = Base64.getEncoder().encodeToString("message".getBytes(StandardCharsets.UTF_8));
        awaitOutput.awaitStdOut(encodedPayload);
        awaitOutput.awaitLog("received PUBLISH ('message')");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showTopics(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-s", "-T");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith().topic("test").payload("message".getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitStdOut("test: message");
        awaitOutput.awaitLog("received PUBLISH ('message')");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_Json(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-s", "-J");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(subscribeCommand).awaitLog("sending SUBSCRIBE").awaitLog("received SUBACK");

        assertSubscribePacket(hivemq.getSubscribePackets().get(0),
                subscribeAssertion -> subscribeAssertion.setSubscriptions(List.of(new SubscriptionImpl("test",
                        Qos.EXACTLY_ONCE,
                        RetainHandling.SEND,
                        false,
                        false))));

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("property1", "value1");
        jsonObject.addProperty("property2", "value2");
        jsonObject.addProperty("property3", "value3");

        publisher.publishWith().topic("test").payload(jsonObject.toString().getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitStdOut(
                "{\n" + "  \"topic\": \"test\",\n" + "  \"payload\": {\n" + "    \"property1\": \"value1\",\n" +
                        "    \"property2\": \"value2\",\n" + "    \"property3\": \"value3\"\n" + "  },\n");
        awaitOutput.awaitStdOut("\"qos\": \"AT_MOST_ONCE\",");
        awaitOutput.awaitStdOut("\"receivedAt\":");
        awaitOutput.awaitStdOut("\"retain\": false");
        awaitOutput.awaitStdOut("}");
        awaitOutput.awaitLog("received PUBLISH");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_subscribeMissingTopic(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdErr("Missing required option: '--topic <topics>'")
                .awaitStdOut("cliTest@" + hivemq.getHost() + ">");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_missingArguments() throws Exception {
        final List<String> subscribeCommand = List.of("sub");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut("mqtt>")
                .awaitStdErr("Unmatched argument at index 0: 'sub'");
    }
}
