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
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShellPublishST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulPublish(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_messageToFile(final char mqttVersion) throws Exception {
        final Path publishFile = Files.createTempFile("publish", "txt");
        Files.write(publishFile, "message".getBytes(StandardCharsets.UTF_8));
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m:file", publishFile.toString());
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopics(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test1", "-t", "test2", "-t", "test3", "-m", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        final PublishPacket publishPacket1 = hivemq.getPublishPackets().get(0);
        final PublishPacket publishPacket2 = hivemq.getPublishPackets().get(1);
        final PublishPacket publishPacket3 = hivemq.getPublishPackets().get(2);
        final Set<String> topicSet =
                Set.of(publishPacket1.getTopic(), publishPacket2.getTopic(), publishPacket3.getTopic());
        assertTrue(topicSet.containsAll(List.of("test1", "test2", "test3")));

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket1.getTopic());
        });

        assertPublishPacket(hivemq.getPublishPackets().get(1), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket2.getTopic());
        });

        assertPublishPacket(hivemq.getPublishPackets().get(2), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket3.getTopic());
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_qos(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "1");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            publishAssertion.setQos(Qos.AT_LEAST_ONCE);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopicsAndMultipleQos(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub",
                "-t",
                "test1",
                "-t",
                "test2",
                "-t",
                "test3",
                "-q",
                "0",
                "-q",
                "1",
                "-q",
                "2",
                "-m",
                "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        final Optional<PublishPacket> optionalPublishPacket1 =
                hivemq.getPublishPackets().stream().filter(publish -> publish.getTopic().equals("test1")).findFirst();
        assertTrue(optionalPublishPacket1.isPresent());
        final PublishPacket publishPacket1 = optionalPublishPacket1.get();
        final Optional<PublishPacket> optionalPublishPacket2 =
                hivemq.getPublishPackets().stream().filter(publish -> publish.getTopic().equals("test2")).findFirst();
        assertTrue(optionalPublishPacket2.isPresent());
        final PublishPacket publishPacket2 = optionalPublishPacket2.get();
        final Optional<PublishPacket> optionalPublishPacket3 =
                hivemq.getPublishPackets().stream().filter(publish -> publish.getTopic().equals("test3")).findFirst();
        assertTrue(optionalPublishPacket3.isPresent());
        final PublishPacket publishPacket3 = optionalPublishPacket3.get();
        final Set<String> topicSet =
                Set.of(publishPacket1.getTopic(), publishPacket2.getTopic(), publishPacket3.getTopic());
        assertTrue(topicSet.containsAll(List.of("test1", "test2", "test3")));

        assertPublishPacket(publishPacket1, publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket1.getTopic());
            publishAssertion.setQos(Qos.AT_MOST_ONCE);
        });

        assertPublishPacket(publishPacket2, publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket2.getTopic());
            publishAssertion.setQos(Qos.AT_LEAST_ONCE);
        });

        assertPublishPacket(publishPacket3, publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic(publishPacket3.getTopic());
            publishAssertion.setQos(Qos.EXACTLY_ONCE);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_retain(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-r");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            publishAssertion.setRetain(true);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_messageExpiryInterval(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-e", "120");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish message expiry was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish message expiry was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
            awaitOutput.awaitLog("sending PUBLISH");
            awaitOutput.awaitLog("finish PUBLISH");
        } else {
            awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
            awaitOutput.awaitLog("sending PUBLISH");
            awaitOutput.awaitLog("finish PUBLISH");
            awaitOutput.awaitLog("messageExpiryInterval=120");
        }

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                publishAssertion.setMessageExpiryInterval(120);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_payloadFormatIndicator(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-pf", "utf8");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish payload format indicator was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish payload format indicator was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending PUBLISH");
        awaitOutput.awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                publishAssertion.setPayloadFormatIndicator(PayloadFormatIndicator.UTF_8);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_contentType(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-ct", "my-content");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish content type was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish content type was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending PUBLISH");
        awaitOutput.awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                publishAssertion.setContentType("my-content");
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_responseTopic(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-rt", "response-topic");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish response topic was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish response topic was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending PUBLISH");
        awaitOutput.awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                publishAssertion.setResponseTopic("response-topic");
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_correlationData(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-cd", "correlation-data");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish correlation data was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish correlation data was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending PUBLISH");
        awaitOutput.awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                publishAssertion.setCorrelationData(ByteBuffer.wrap("correlation-data".getBytes(StandardCharsets.UTF_8)));
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> publishCommand =
                List.of("pub", "-t", "test", "-m", "test", "-up", "key1=value1", "-up", "key2=value2");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(publishCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Publish user properties were set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Publish user properties were set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending PUBLISH");
        awaitOutput.awaitLog("finish PUBLISH");

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setPayload(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)));
            publishAssertion.setTopic("test");
            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties =
                        UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder()
                                .add(new MqttUserProperty("key1", "value1"))
                                .add(new MqttUserProperty("key2", "value2"))
                                .build());
                publishAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_publishMissingTopic(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdErr("Missing required option: '--topic <topics>'")
                .awaitStdOut("cliTest@" + hivemq.getHost() + ">");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_publishMissingMessage(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub", "-t", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdErr("Error: Missing required argument (specify one of these)")
                .awaitStdOut("cliTest@" + hivemq.getHost() + ">");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_missingArguments() throws Exception {
        final List<String> publishCommand = List.of("pub");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdErr("Unmatched argument at index 0: 'pub'")
                .awaitStdOut("mqtt>");
    }
}
