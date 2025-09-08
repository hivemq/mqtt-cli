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
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
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

import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscribeDeprecationST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties_legacyInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-up");
        subscribeCommand.add("key1=value1");
        subscribeCommand.add("-up");
        subscribeCommand.add("key2=value2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "Options \"-up\" and \"--userProperty\" are legacy, please use \"--user-property\". Legacy options will be removed in a future version.");

        assertSubscribe(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Subscribe user properties were set but are unused in MQTT version MQTT_3_1_1");
        }

        publishMessage("message");

        executionResult.awaitStdOut("message");

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));

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
    void test_userProperties_mixedLegacyAndCurrent(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-up");
        subscribeCommand.add("key1=value1");
        subscribeCommand.add("--user-property");
        subscribeCommand.add("key2=value2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "A mix of the user properties legacy options \"-up\" or \"--userProperty\" and the current \"--user-property\" is used. Please only use \"--user-property\" as the legacy options will be removed in a future version.");

        final List<String> subscribeCommand2 = defaultSubscribeCommand(mqttVersion);
        subscribeCommand2.add("--user-property");
        subscribeCommand2.add("key2=value2");
        subscribeCommand2.add("-up");
        subscribeCommand2.add("key1=value1");

        final ExecutionResultAsync executionResult2 = mqttCli.executeAsync(subscribeCommand2);

        executionResult2.awaitStdErr(
                "A mix of the user properties legacy options \"-up\" or \"--userProperty\" and the current \"--user-property\" is used. Please only use \"--user-property\" as the legacy options will be removed in a future version.");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_outputFile_legacyInformation(final char mqttVersion) throws Exception {
        final Path messageFile = Files.createTempFile("message-file", ".txt");
        messageFile.toFile().deleteOnExit();

        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-of");
        subscribeCommand.add(messageFile.toString());

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "Options \"-of\" and \"--outputToFile\" are legacy, please use \"--output-to-file\". Legacy options will be removed in a future version.");

        assertSubscribe(executionResult);

        publishMessage("message");

        executionResult.awaitStdOut("message");

        final List<String> readLines = Files.readAllLines(messageFile);
        assertEquals(1, readLines.size());
        assertEquals("message", readLines.getFirst());

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_outputFile_mixedLegacyAndCurrent(final char mqttVersion) throws Exception {
        final Path messageFile = Files.createTempFile("message-file", ".txt");
        messageFile.toFile().deleteOnExit();

        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-of");
        subscribeCommand.add(messageFile.toString());
        subscribeCommand.add("--output-to-file");
        subscribeCommand.add(messageFile.toString());

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdErr(
                "A mix of the output file legacy options \"-of\" or \"--outputToFile\" and the current \"--output-to-file\" is used. Please only use \"--output-to-file\" as the legacy options will be removed in a future version.");

        final List<String> subscribeCommand2 = defaultSubscribeCommand(mqttVersion);
        subscribeCommand2.add("-of");
        subscribeCommand2.add(messageFile.toString());
        subscribeCommand2.add("--output-to-file");
        subscribeCommand2.add(messageFile.toString());

        final ExecutionResultAsync executionResult2 = mqttCli.executeAsync(subscribeCommand2);
        executionResult2.awaitStdErr(
                "A mix of the output file legacy options \"-of\" or \"--outputToFile\" and the current \"--output-to-file\" is used. Please only use \"--output-to-file\" as the legacy options will be removed in a future version.");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_base64_legacyInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-b64");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "Option \"-b64\" is legacy, please use \"--base64\". The legacy option will be removed in a future version.");

        assertSubscribe(executionResult);

        publishMessage("message");

        final String encodedPayload = Base64.getEncoder().encodeToString("message".getBytes(StandardCharsets.UTF_8));
        executionResult.awaitStdOut(encodedPayload);

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_base64_mixedLegacyAndCurrent(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-b64");
        subscribeCommand.add("--base64");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdErr(
                "A mix of the base64 legacy options \"-b64\" and the current \"--base64\" is used. Please only use \"--base64\" as the legacy options will be removed in a future version.");

        final List<String> subscribeCommand2 = defaultSubscribeCommand(mqttVersion);
        subscribeCommand2.add("--base64");
        subscribeCommand2.add("-b64");

        final ExecutionResultAsync executionResult2 = mqttCli.executeAsync(subscribeCommand2);
        executionResult2.awaitStdErr(
                "A mix of the base64 legacy options \"-b64\" and the current \"--base64\" is used. Please only use \"--base64\" as the legacy options will be removed in a future version.");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showJson_legacyInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--jsonOutput");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "Option \"--jsonOutput\" is legacy, please use \"--json-output\". The legacy option will be removed in a future version.");

        assertSubscribe(executionResult);

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("property1", "value1");
        jsonObject.addProperty("property2", "value2");
        jsonObject.addProperty("property3", "value3");
        publishMessage(jsonObject.toString());

        executionResult.awaitStdOut("{\n" +
                "  \"topic\": \"topic\",\n" +
                "  \"payload\": {\n" +
                "    \"property1\": \"value1\",\n" +
                "    \"property2\": \"value2\",\n" +
                "    \"property3\": \"value3\"\n" +
                "  },\n");
        executionResult.awaitStdOut("\"qos\": \"EXACTLY_ONCE\",");
        executionResult.awaitStdOut("\"receivedAt\":");
        executionResult.awaitStdOut("\"retain\": false");
        executionResult.awaitStdOut("}");

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showJson_mixedLegacyAndCurrent(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--jsonOutput");
        subscribeCommand.add("--json-output");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdErr(
                "A mix of the json output legacy options \"--jsonOutput\" and the current \"-J\" or \"--json-output\" is used. Please only use \"-J\" or \"--json-output\" as the legacy options will be removed in a future version.");

        final List<String> subscribeCommand2 = defaultSubscribeCommand(mqttVersion);
        subscribeCommand2.add("--json-output");
        subscribeCommand2.add("--jsonOutput");

        final ExecutionResultAsync executionResult2 = mqttCli.executeAsync(subscribeCommand2);
        executionResult2.awaitStdErr(
                "A mix of the json output legacy options \"--jsonOutput\" and the current \"-J\" or \"--json-output\" is used. Please only use \"-J\" or \"--json-output\" as the legacy options will be removed in a future version.");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showTopic_legacyInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--showTopics");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr(
                "Option \"--showTopics\" is legacy, please use \"-T\" or \"--show-topics\". The legacy option will be removed in a future version.");

        assertSubscribe(executionResult);

        publishMessage("message");

        executionResult.awaitStdOut("topic: message");

        assertSubscribePacket(hivemq.getSubscribePackets().getFirst(), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_showTopic_mixedLegacyAndCurrent(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--showTopics");
        subscribeCommand.add("--show-topics");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdErr(
                "A mix of the show topics legacy options \"--showTopics\" and the current \"-T\" or \"--show-topics\" is used. Please only use \"-T\" or \"--show-topics\" as the legacy options will be removed in a future version.");

        final List<String> subscribeCommand2 = defaultSubscribeCommand(mqttVersion);
        subscribeCommand2.add("--show-topics");
        subscribeCommand2.add("--showTopics");

        final ExecutionResultAsync executionResult2 = mqttCli.executeAsync(subscribeCommand2);
        executionResult2.awaitStdErr(
                "A mix of the show topics legacy options \"--showTopics\" and the current \"-T\" or \"--show-topics\" is used. Please only use \"-T\" or \"--show-topics\" as the legacy options will be removed in a future version.");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleLegacyInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--showTopics");
        subscribeCommand.add("--jsonOutput");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdErr("There are deprecated options used in this command:");
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

    private void publishMessage(final @NotNull String message) {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("topic")
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }

}
