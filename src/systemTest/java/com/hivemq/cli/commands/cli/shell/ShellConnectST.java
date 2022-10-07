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

package com.hivemq.cli.commands.cli.shell;

import com.google.common.collect.ImmutableList;
import com.hivemq.cli.utils.AwaitOutput;
import com.hivemq.cli.utils.HiveMQ;
import com.hivemq.cli.utils.MqttCliShell;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.general.MqttVersion;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.ConnectAssertion.assertConnectPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ShellConnectST {

    @RegisterExtension
    static HiveMQ hivemq = new HiveMQ();

    @RegisterExtension
    final MqttCliShell mqttCliShell = new MqttCliShell();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenHelpOptionIsUsed_thenUsageHelpIsDisplayed() throws Exception {
        final List<String> connectCommand = List.of("con", "--help");
        mqttCliShell.executeAsync(connectCommand).awaitStdOut("Usage").awaitStdOut("Options").awaitStdOut("mqtt>");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenConnect_thenConnectIsSuccess(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        final ConnectPacket connectPacket = hivemq.getConnectPackets().get(0);
        assertConnectPacket(connectPacket, connectAssertion -> connectAssertion.setMqttVersion(toVersion(mqttVersion)));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenWrongPortIsUsed_thenConnectIsFailure(final char mqttVersion) throws Exception {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", "22", "-V", String.valueOf(mqttVersion), "-i", "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("Connection refused")
                .awaitStdOut("mqtt>")
                .awaitLog("Connection refused");

        assertEquals(0, hivemq.getConnectPackets().size());
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenWrongHostIsUsed_thenConnectIsFailure(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                "unreachable-host",
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("nodename nor servname provided, or not known")
                .awaitStdOut("mqtt>")
                .awaitLog("nodename nor servname provided, or not known");

        assertEquals(0, hivemq.getConnectPackets().size());
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenNoCleanStartIsUsed_thenConnectCleanStartIsFalse(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--no-cleanStart");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            connectAssertion.setCleanStart(false);
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenReceiveMaximumIsUsed_thenConnectContainsReceiveMaximum(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--rcvMax");
        connectCommand.add("500");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction receive maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setReceiveMaximum(500);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenMaxPacketSizeIsUsed_thenConnectPacketContainsMaxPacketSize(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--maxPacketSize");
        connectCommand.add("500");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction maximum packet size was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setMaximumPacketSize(500);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenTopicAliasMaxIsUsed_thenConnectContainsTopicAliasMax(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--topicAliasMax");
        connectCommand.add("5");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Restriction topic alias maximum was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setTopicAliasMaximum(5);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenRequestProblemInformationIsUSed_thenConnectContainsRequestProblemInformation(final char mqttVersion)
            throws IOException {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--no-reqProblemInfo");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr(
                    "Restriction request problem information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setRequestProblemInformation(false);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenRequestResponseInformationIsUsed_thenConnectContainsRequestResponseInformation(final char mqttVersion)
            throws IOException {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("--reqResponseInfo");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr(
                    "Restriction request response information was set but is unused in MQTT Version MQTT_3_1_1");
        }

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            if (mqttVersion == '5') {
                connectAssertion.setRequestResponseInformation(true);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenNoClientIdIsUsed_thenConnectIsSuccess(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
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
    void whenUserNameIsUsed_thenConnectPacketContainsUserName(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-u");
        connectCommand.add("username");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            connectAssertion.setUserName("username");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenPasswordIsUsed_thenConnectPacketContainsPassword(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-pw");
        connectCommand.add("password");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
        } else {
            awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()))
                    .awaitLog("sending CONNECT")
                    .awaitLog("received CONNACK");
            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(toVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenWillIsUsed_thenConnectContainsWill(final char mqttVersion) throws Exception {
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

            assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                final WillPublishBuilder expectedWillBuilder = Builders.willPublish()
                        .payload(ByteBuffer.wrap("will-message".getBytes(StandardCharsets.UTF_8)))
                        .topic("test-will-topic")
                        .qos(Qos.EXACTLY_ONCE)
                        .messageExpiryInterval(4294967295L)
                        .retain(true);

                connectAssertion.setMqttVersion(toVersion(mqttVersion));
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

                connectAssertion.setMqttVersion(toVersion(mqttVersion));
                connectAssertion.setWillPublish(expectedWillBuilder.build());
            });
        }

    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenSessionExpiryIsUsed_thenSessionIsUsed() throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-se",
                "120",
                "-i",
                "sessionTest",
                "--no-cleanStart");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("sessionTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("sessionExpiryInterval=120")
                .awaitLog("received CONNACK")
                .awaitLog("sessionPresent=false");

        final ConnectPacket connectPacket1 = hivemq.getConnectPackets().get(0);
        assertConnectPacket(connectPacket1, connectAssertion -> {
            connectAssertion.setClientId("sessionTest");
            connectAssertion.setCleanStart(false);
            connectAssertion.setSessionExpiryInterval(120);
        });

        mqttCliShell.executeAsync(List.of("dis")).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("sessionTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("sessionExpiryInterval=120")
                .awaitLog("received CONNACK")
                .awaitLog("sessionPresent=true");

        final ConnectPacket connectPacket2 = hivemq.getConnectPackets().get(1);
        assertConnectPacket(connectPacket2, connectAssertion -> {
            connectAssertion.setClientId("sessionTest");
            connectAssertion.setCleanStart(false);
            connectAssertion.setSessionExpiryInterval(120);
        });
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenIdentifierPrefixIsUsed_thenAssignedClientIdStartsWithPrefix() throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-ip",
                "myPrefix");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut("myPrefix")
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenUserPropertyIsUsed_thenConnectSentWithUserProperties() throws Exception {
        final List<String> connectCommand = defaultConnectCommand();
        connectCommand.add("-up");
        connectCommand.add("key1=value1");
        connectCommand.add("-up");
        connectCommand.add("key2=value2");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("userProperties=[(key1, value1), (key2, value2)]")
                .awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            final ImmutableList<MqttUserProperty> userProperties = ImmutableList.<MqttUserProperty>builder()
                    .add(new MqttUserProperty("key1", "value1"))
                    .add(new MqttUserProperty("key2", "value2"))
                    .build();

            connectAssertion.setUserProperties(UserPropertiesImpl.of(userProperties));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void whenKeepAliveIsUsed_thenConnectContainsKeepAlive(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
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
            connectAssertion.setMqttVersion(toVersion(mqttVersion));
            connectAssertion.setKeepAlive(30);
        });
    }

    private @NotNull MqttVersion toVersion(final char version) {
        if (version == '3') {
            return MqttVersion.V_3_1_1;
        } else if (version == '5') {
            return MqttVersion.V_5;
        }
        fail("version " + version + " can not be converted to MqttVersion object.");
        throw new RuntimeException();
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
