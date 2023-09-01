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
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.extension.sdk.api.packets.connack.ConnackPacket;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PayloadFormatIndicator;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.builder.WillPublishBuilder;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeConnectST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();


    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_WrongHost(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                "wrong-host",
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-t",
                "test",
                "-d");

        final ExecutionResult executionResult = MqttCli.execute(subscribeCommand);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Unable to connect"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_WrongPort(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                "22",
                "-V",
                String.valueOf(mqttVersion),
                "-t",
                "test",
                "-d");

        final ExecutionResult executionResult = MqttCli.execute(subscribeCommand);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Unable to connect"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_NoCleanStart(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--no-cleanStart");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setCleanStart(false);
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(4294967295L);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_CleanStart(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--cleanStart");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setCleanStart(true);
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(0L);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_KeepAlive(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-k");
        subscribeCommand.add("100");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setKeepAlive(100);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_NoClientId(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.remove("-i");
        subscribeCommand.remove("cliTest");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("received CONNACK");

        final String expectedClientId;
        if (mqttVersion == '5') {
            final ConnackPacket connackPacket = hivemq.getConnackPackets().get(0);
            assertTrue(connackPacket.getAssignedClientIdentifier().isPresent());
            expectedClientId = connackPacket.getAssignedClientIdentifier().get();
        } else {
            final ConnectPacket connectPacket = hivemq.getConnectPackets().get(0);
            expectedClientId = connectPacket.getClientId();
        }

        executionResult.awaitStdOut(String.format("Client '%s@%s' sending SUBSCRIBE",
                expectedClientId,
                hivemq.getHost()));
        executionResult.awaitStdOut(String.format("Client '%s@%s' received SUBACK",
                expectedClientId,
                hivemq.getHost()));

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setClientId(expectedClientId);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_IdentifierPrefix(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.remove("-i");
        subscribeCommand.remove("cliTest");
        subscribeCommand.add("-ip");
        subscribeCommand.add("test-");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        final ConnectPacket connectPacket = hivemq.getConnectPackets().get(0);
        if (mqttVersion == '3') {
            assertTrue(connectPacket.getClientId().startsWith("test-"));
        }

        assertConnectPacket(connectPacket, connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setClientId(connectPacket.getClientId());
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_UserName(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-u");
        subscribeCommand.add("username");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_Password(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-pw");
        subscribeCommand.add("password");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
        } else {
            assertSubscribeOutput(executionResult);
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_PasswordEnv(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-pw:env");
        subscribeCommand.add("PASSWORD");

        final ExecutionResultAsync executionResult =
                mqttCli.executeAsync(subscribeCommand, Map.of("PASSWORD", "password"));

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
        } else {
            assertSubscribeOutput(executionResult);
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_PasswordFile(final char mqttVersion) throws Exception {
        final Path passwordFile = Files.createTempFile("password-file", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password");

        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-pw:file");
        subscribeCommand.add(passwordFile.toString());

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
        } else {
            assertSubscribeOutput(executionResult);
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_UserNameAndPassword(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-u");
        subscribeCommand.add("username");
        subscribeCommand.add("-pw");
        subscribeCommand.add("password");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_UserNameAndPasswordEnv(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-u");
        subscribeCommand.add("username");
        subscribeCommand.add("-pw:env");
        subscribeCommand.add("PASSWORD");

        final ExecutionResultAsync executionResult =
                mqttCli.executeAsync(subscribeCommand, Map.of("PASSWORD", "password"));

        assertSubscribeOutput(executionResult);
        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_UserNamePasswordFile(final char mqttVersion) throws Exception {
        final Path passwordFile = Files.createTempFile("password-file", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password");

        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-u");
        subscribeCommand.add("username");
        subscribeCommand.add("-pw:file");
        subscribeCommand.add(passwordFile.toString());

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        assertSubscribeOutput(executionResult);
        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_Will(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-Wt");
        subscribeCommand.add("test-will-topic");
        subscribeCommand.add("-Wm");
        subscribeCommand.add("will-message");
        subscribeCommand.add("-Wq");
        subscribeCommand.add("2");
        subscribeCommand.add("-Wr");
        subscribeCommand.add("-We");
        subscribeCommand.add("120");
        subscribeCommand.add("-Wd");
        subscribeCommand.add("180");
        subscribeCommand.add("-Wpf");
        subscribeCommand.add(PayloadFormatIndicator.UTF_8.name());
        subscribeCommand.add("-Wct");
        subscribeCommand.add("content-type");
        subscribeCommand.add("-Wrt");
        subscribeCommand.add("will-response-topic");
        subscribeCommand.add("-Wup");
        subscribeCommand.add("key1=value1");
        subscribeCommand.add("-Wup");
        subscribeCommand.add("key2=value2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Will Message Expiry was set but is unused in MQTT Version MQTT_3_1_1");
            executionResult.awaitStdErr("Will Payload Format was set but is unused in MQTT Version MQTT_3_1_1");
            executionResult.awaitStdErr("Will Delay Interval was set but is unused in MQTT Version MQTT_3_1_1");
            executionResult.awaitStdErr("Will Content Type was set but is unused in MQTT Version MQTT_3_1_1");
            executionResult.awaitStdErr("Will Response Topic was set but is unused in MQTT Version MQTT_3_1_1");
            executionResult.awaitStdErr("Will User Properties was set but is unused in MQTT Version MQTT_3_1_1");

            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                final WillPublishBuilder expectedWillBuilder = Builders.willPublish()
                        .payload(ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)))
                        .topic("test-will-topic")
                        .qos(Qos.EXACTLY_ONCE)
                        .messageExpiryInterval(4294967295L)
                        .retain(true);

                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setWillPublish(expectedWillBuilder.build());
            });

        } else {
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                final WillPublishBuilder expectedWillBuilder = Builders.willPublish()
                        .payload(ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)))
                        .topic("test-will-topic")
                        .qos(Qos.EXACTLY_ONCE)
                        .retain(true)
                        .payloadFormatIndicator(PayloadFormatIndicator.UNSPECIFIED)
                        .messageExpiryInterval(120)
                        .willDelay(180)
                        .payloadFormatIndicator(PayloadFormatIndicator.UTF_8)
                        .contentType("content-type")
                        .responseTopic("will-response-topic")
                        .userProperty("key1", "value1")
                        .userProperty("key2", "value2");

                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setWillPublish(expectedWillBuilder.build());
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_ReceiveMax(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--rcvMax");
        subscribeCommand.add("100");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Restriction receive maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setReceiveMaximum(100);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_MaxPacketSize(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--maxPacketSize");
        subscribeCommand.add("100");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr(
                    "Restriction maximum packet size was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setMaximumPacketSize(100);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_TopicAliasMaximum(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--topicAliasMax");
        subscribeCommand.add("100");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr(
                    "Restriction topic alias maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setTopicAliasMaximum(100);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_NoRequestProblemInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--no-reqProblemInfo");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setRequestProblemInformation(false);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_RequestProblemInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--reqProblemInfo");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr(
                    "Restriction request problem information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setRequestProblemInformation(true);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_RequestResponseInformation(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("--reqResponseInfo");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr(
                    "Restriction request response information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setRequestResponseInformation(true);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_SessionExpiryInterval(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-se");
        subscribeCommand.add("100");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr(
                    "Connect session expiry interval was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setSessionExpiryInterval(100);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connect_UserProperties(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = defaultSubscribeCommand(mqttVersion);
        subscribeCommand.add("-Cup");
        subscribeCommand.add("key1=value1");
        subscribeCommand.add("-Cup");
        subscribeCommand.add("key2=value2");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        assertSubscribeOutput(executionResult);

        if (mqttVersion == '3') {
            executionResult.awaitStdErr("Connect user properties were set but are unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties = UserPropertiesImpl.of(ImmutableList.of(
                        MqttUserProperty.of("key1", "value1"),
                        MqttUserProperty.of("key2", "value2")));
                connectAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }


    private void assertSubscribeOutput(final @NotNull ExecutionResultAsync executionResultAsync) {
        executionResultAsync.awaitStdOut("sending CONNECT");
        executionResultAsync.awaitStdOut("received CONNACK");
        executionResultAsync.awaitStdOut("sending SUBSCRIBE");
        executionResultAsync.awaitStdOut("received SUBACK");
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
        subscribeCommand.add("test");
        subscribeCommand.add("-d");
        return subscribeCommand;
    }
}
