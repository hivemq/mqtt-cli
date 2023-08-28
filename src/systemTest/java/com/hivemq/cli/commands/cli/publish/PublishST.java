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

package com.hivemq.cli.commands.cli.publish;

import com.google.common.collect.ImmutableList;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PayloadFormatIndicator;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulConnectAndPublish(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertPublishOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_retain(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-r");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setRetain(true);
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_messageFromFile(final char mqttVersion) throws Exception {
        final Path messageFile = Files.createTempFile("message-file", ".txt");
        Files.writeString(messageFile, "message-from-file");

        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.remove("-m");
        publishCommand.remove("message");
        publishCommand.add("-m:file");
        publishCommand.add(messageFile.toString());

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message-from-file".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_emptyMessage(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.remove("-m");
        publishCommand.remove("message");
        publishCommand.add("-m:empty");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.allocate(0));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopics(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.remove("-t");
        publishCommand.remove("test");
        publishCommand.add("-t");
        publishCommand.add("test1");
        publishCommand.add("-t");
        publishCommand.add("test2");
        publishCommand.add("-t");
        publishCommand.add("test3");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish{topic=test1"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish{topic=test2"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish{topic=test3"));

        final Set<String> topicSet =
                hivemq.getPublishPackets().stream().map(PublishPacket::getTopic).collect(Collectors.toSet());
        assertTrue(topicSet.containsAll(List.of("test1", "test2", "test3")));

        for (final PublishPacket publishPacket : hivemq.getPublishPackets()) {
            assertPublishPacket(publishPacket, publishAssertion -> {
                publishAssertion.setTopic(publishPacket.getTopic());
                publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopicsMultipleQos(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.remove("-t");
        publishCommand.remove("test");
        publishCommand.add("-t");
        publishCommand.add("test1");
        publishCommand.add("-t");
        publishCommand.add("test2");
        publishCommand.add("-t");
        publishCommand.add("test3");
        publishCommand.add("-q");
        publishCommand.add("0");
        publishCommand.add("-q");
        publishCommand.add("1");
        publishCommand.add("-q");
        publishCommand.add("2");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput()
                .contains("MqttPublish{topic=test1, payload=7byte, qos=AT_MOST_ONCE, retain=false}"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput()
                .contains("MqttPublish{topic=test2, payload=7byte, qos=AT_LEAST_ONCE, retain=false}"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH ('message')"));
        assertTrue(executionResult.getStandardOutput()
                .contains("MqttPublish{topic=test3, payload=7byte, qos=EXACTLY_ONCE, retain=false}"));

        final Map<String, PublishPacket> topicToPublishPacket = hivemq.getPublishPackets()
                .stream()
                .collect(Collectors.toMap(PublishPacket::getTopic, publishPacket -> publishPacket));

        assertEquals(Qos.AT_MOST_ONCE, topicToPublishPacket.get("test1").getQos());
        assertEquals(Qos.AT_LEAST_ONCE, topicToPublishPacket.get("test2").getQos());
        assertEquals(Qos.EXACTLY_ONCE, topicToPublishPacket.get("test3").getQos());

        for (final PublishPacket publishPacket : hivemq.getPublishPackets()) {
            assertPublishPacket(publishPacket, publishAssertion -> {
                publishAssertion.setTopic(publishPacket.getTopic());
                publishAssertion.setQos(publishPacket.getQos());
                publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_messageExpiryInterval(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-e");
        publishCommand.add("60");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish message expiry was set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                publishAssertion.setMessageExpiryInterval(60);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_payloadFormatIndicator(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-pf");
        publishCommand.add("utf8");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish payload format indicator was set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                publishAssertion.setPayloadFormatIndicator(PayloadFormatIndicator.UTF_8);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_contentType(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-ct");
        publishCommand.add("content-type");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish content type was set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                publishAssertion.setContentType("content-type");
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_responseTopic(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-rt");
        publishCommand.add("response-topic");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish response topic was set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                publishAssertion.setResponseTopic("response-topic");
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_correlationData(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-cd");
        publishCommand.add("correlation-data");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish correlation data was set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                publishAssertion.setCorrelationData(ByteBuffer.wrap("correlation-data".getBytes(StandardCharsets.UTF_8)));
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> publishCommand = defaultPublishCommand(mqttVersion);
        publishCommand.add("-up");
        publishCommand.add("key1=value1");
        publishCommand.add("-up");
        publishCommand.add("key2=value2");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertPublishOutput(executionResult);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Publish user properties were set but is unused in MQTT Version MQTT_3_1_1"));
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties = UserPropertiesImpl.of(ImmutableList.of(
                        MqttUserProperty.of("key1", "value1"),
                        MqttUserProperty.of("key2", "value2")));
                publishAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_missing_topic() throws Exception {
        final List<String> publishCommand =
                List.of("pub", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()));

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Missing required option: '--topic=<topics>'"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_missing_message() throws Exception {
        final List<String> publishCommand =
                List.of("pub", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()), "-t", "test");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Missing required argument (specify one of these):"));
    }

    private void assertPublishOutput(final @NotNull ExecutionResult executionResult) {
        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));
    }

    private @NotNull List<String> defaultPublishCommand(final char mqttVersion) {
        final ArrayList<String> publishCommand = new ArrayList<>();
        publishCommand.add("pub");
        publishCommand.add("-h");
        publishCommand.add(hivemq.getHost());
        publishCommand.add("-p");
        publishCommand.add(String.valueOf(hivemq.getMqttPort()));
        publishCommand.add("-V");
        publishCommand.add(String.valueOf(mqttVersion));
        publishCommand.add("-i");
        publishCommand.add("cliTest");
        publishCommand.add("-t");
        publishCommand.add("test");
        publishCommand.add("-m");
        publishCommand.add("message");
        publishCommand.add("-d");
        return publishCommand;
    }
}
