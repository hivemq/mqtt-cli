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

package com.hivemq.cli.commands.shell.connect;

import com.google.common.collect.ImmutableList;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.publish.PayloadFormatIndicator;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.builder.WillPublishBuilder;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellConnectST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();


    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_help() throws Exception {
        final List<String> connectCommand = List.of("con", "--help");
        mqttCliShell.executeAsync(connectCommand).awaitStdOut("Usage").awaitStdOut("OPTIONS").awaitStdOut("mqtt>");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_defaultConnect(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        final ConnectPacket connectPacket = hivemq.getConnectPackets().get(0);
        assertConnectPacket(connectPacket,
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectWhileConnected(final char mqttVersion) throws Exception {
        final List<String> connectCommand1 = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "client1");

        final List<String> connectCommand2 = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "client2");

        mqttCliShell.executeAsync(connectCommand1)
                .awaitStdOut(String.format("client1@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        final ConnectPacket connectPacket1 = hivemq.getConnectPackets().get(0);
        assertConnectPacket(connectPacket1, connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setClientId("client1");
        });

        mqttCliShell.executeAsync(connectCommand2)
                .awaitStdOut(String.format("client2@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        final ConnectPacket connectPacket2 = hivemq.getConnectPackets().get(1);
        assertConnectPacket(connectPacket2, connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setClientId("client2");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_wrongPort(final char mqttVersion) throws Exception {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", "22", "-V", String.valueOf(mqttVersion), "-i", "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("Unable to connect")
                .awaitStdOut("mqtt>")
                .awaitLog("Unable to connect");

        assertEquals(0, hivemq.getConnectPackets().size());
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_wrongHost(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of("con",
                "-h",
                "unreachable-host",
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("Unable to connect")
                .awaitStdOut("mqtt>")
                .awaitLog("Unable to connect");

        assertEquals(0, hivemq.getConnectPackets().size());
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_noCleanStart(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--no-cleanStart");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(4294967295L);
            }
            connectAssertion.setCleanStart(false);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_cleanStart(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--cleanStart");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(0L);
            }
            connectAssertion.setCleanStart(true);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_receiveMaximum(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--rcvMax");
        connectCommand.add("500");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction receive maximum was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Restriction receive maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setReceiveMaximum(500);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_maxPacketSize(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--maxPacketSize");
        connectCommand.add("500");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction maximum packet size was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Restriction maximum packet size was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setMaximumPacketSize(500);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_topicAliasMaximum(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--topicAliasMax");
        connectCommand.add("5");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction topic alias maximum was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Restriction topic alias maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setTopicAliasMaximum(5);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_noRequestProblemInformation(final char mqttVersion) throws IOException {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--no-reqProblemInfo");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

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
    void test_requestProblemInformation(final char mqttVersion) throws IOException {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--reqProblemInfo");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr(
                    "Restriction request problem information was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog(
                    "Restriction request problem information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

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
    void test_requestResponseInformation(final char mqttVersion) throws IOException {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--reqResponseInfo");

        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("@%s>", hivemq.getHost()));

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr(
                    "Restriction request response information was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog(
                    "Restriction request response information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

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
    void test_noClientId(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion));

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '5') {
            awaitOutput.awaitLog("assignedClientIdentifier=");
        }

        assertEquals(1, hivemq.getConnectPackets().size());
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userName(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-u");
        connectCommand.add("username");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_password(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-pw");
        connectCommand.add("password");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
            awaitOutput.awaitLog("Password-Only Authentication is not allowed in MQTT 3");
            awaitOutput.awaitStdOut("mqtt>");
        } else {
            awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("received CONNACK");
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userNameAndPassword(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-u");
        connectCommand.add("user");
        connectCommand.add("-pw");
        connectCommand.add("password");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("user");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_passwordFile(final char mqttVersion) throws Exception {
        final Path passwordFile = Files.createTempFile("mqtt-cli-password", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password", StandardCharsets.UTF_8);

        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-pw:file");
        connectCommand.add(passwordFile.toString());

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
            awaitOutput.awaitLog("Password-Only Authentication is not allowed in MQTT 3");
            awaitOutput.awaitStdOut("mqtt>");
        } else {
            awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("received CONNACK");
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userNameAndPasswordFile(final char mqttVersion) throws Exception {

        final Path passwordFile = Files.createTempFile("mqtt-cli-password", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password", StandardCharsets.UTF_8);

        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-u");
        connectCommand.add("user");
        connectCommand.add("-pw:file");
        connectCommand.add(passwordFile.toString());

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("user");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_will(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-Wt");
        connectCommand.add("test-will-topic");
        connectCommand.add("-Wm");
        connectCommand.add("will-message");
        connectCommand.add("-Wq");
        connectCommand.add("2");
        connectCommand.add("-Wr");
        connectCommand.add("-We");
        connectCommand.add("120");
        connectCommand.add("-Wd");
        connectCommand.add("180");
        connectCommand.add("-Wpf");
        connectCommand.add(PayloadFormatIndicator.UTF_8.name());
        connectCommand.add("-Wct");
        connectCommand.add("content-type");
        connectCommand.add("-Wrt");
        connectCommand.add("will-response-topic");
        connectCommand.add("-Wup");
        connectCommand.add("key1=value1");
        connectCommand.add("-Wup");
        connectCommand.add("key2=value2");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitLog("Will Message Expiry was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Will Payload Format was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Will Delay Interval was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Will Content Type was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Will Response Topic was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Will User Properties was set but is unused in MQTT Version MQTT_3_1_1");

            awaitOutput.awaitStdErr("Will Message Expiry was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdErr("Will Payload Format was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdErr("Will Delay Interval was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdErr("Will Content Type was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdErr("Will Response Topic was set but is unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitStdErr("Will User Properties was set but is unused in MQTT Version MQTT_3_1_1");

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
            awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("received CONNACK");

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
    void test_sessionExpiryInterval(final char mqttVersion) throws Exception {
        final String clientId = "sessionTest_V" + mqttVersion;
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-se",
                "120",
                "-i",
                clientId,
                "--no-cleanStart");

        if (mqttVersion == '3') {
            mqttCliShell.executeAsync(connectCommand)
                    .awaitStdOut(String.format("%s@%s>", clientId, hivemq.getHost()))
                    .awaitStdErr("Connect session expiry interval was set but is unused in MQTT Version MQTT_3_1_1")
                    .awaitLog("sending CONNECT")
                    .awaitLog("received CONNACK");

            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setSessionExpiryInterval(4294967295L);
                connectAssertion.setCleanStart(false);
                connectAssertion.setClientId(clientId);
            });
        } else {
            mqttCliShell.executeAsync(connectCommand)
                    .awaitStdOut(String.format("%s@%s>", clientId, hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("sessionExpiryInterval=120")
                    .awaitLog("received CONNACK")
                    .awaitLog("sessionPresent=false");

            final ConnectPacket connectPacket1 = hivemq.getConnectPackets().get(0);
            assertConnectPacket(connectPacket1, connectAssertion -> {
                connectAssertion.setClientId(clientId);
                connectAssertion.setCleanStart(false);
                connectAssertion.setSessionExpiryInterval(120);
            });

            mqttCliShell.executeAsync(List.of("dis")).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

            mqttCliShell.executeAsync(connectCommand)
                    .awaitStdOut(String.format("%s@%s>", clientId, hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("sessionExpiryInterval=120")
                    .awaitLog("received CONNACK")
                    .awaitLog("sessionPresent=true");

            final ConnectPacket connectPacket2 = hivemq.getConnectPackets().get(1);
            assertConnectPacket(connectPacket2, connectAssertion -> {
                connectAssertion.setClientId(clientId);
                connectAssertion.setCleanStart(false);
                connectAssertion.setSessionExpiryInterval(120);
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_identifierPrefix(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-ip",
                "myPrefix");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdOut("myPrefix");
        }

        awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-Cup");
        connectCommand.add("key1=value1");
        connectCommand.add("-Cup");
        connectCommand.add("key2=value2");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Connect user properties were set but are unused in MQTT Version MQTT_3_1_1");
        }

        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending CONNECT");

        if (mqttVersion == '5') {
            awaitOutput.awaitLog("userProperties=[(key1, value1), (key2, value2)]");
        }

        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '5') {
                final ImmutableList<MqttUserProperty> userProperties = ImmutableList.<MqttUserProperty>builder()
                        .add(new MqttUserProperty("key1", "value1"))
                        .add(new MqttUserProperty("key2", "value2"))
                        .build();
                connectAssertion.setUserProperties(UserPropertiesImpl.of(userProperties));
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_keepAlive(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-i",
                "cliTest",
                "-V",
                String.valueOf(mqttVersion),
                "-k",
                "30");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("keepAlive=30")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setKeepAlive(30);
        });
    }


    private @NotNull List<String> defaultConnectCommand() {
        final ArrayList<String> defaultConnectCommand = new ArrayList<>();
        defaultConnectCommand.add("con");
        defaultConnectCommand.add("-h");
        defaultConnectCommand.add(hivemq.getHost());
        defaultConnectCommand.add("-p");
        defaultConnectCommand.add(String.valueOf(hivemq.getMqttPort()));
        defaultConnectCommand.add("-i");
        defaultConnectCommand.add("cliTest");
        return defaultConnectCommand;
    }

    private @NotNull List<String> defaultConnectCommand(final char mqttVersion) {
        final List<String> defaultConnectCommand = defaultConnectCommand();
        defaultConnectCommand.add("-V");
        defaultConnectCommand.add(String.valueOf(mqttVersion));
        return defaultConnectCommand;
    }
}
