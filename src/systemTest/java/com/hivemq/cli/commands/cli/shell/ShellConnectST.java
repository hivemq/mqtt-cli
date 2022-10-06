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

import com.hivemq.cli.utils.AwaitOutput;
import com.hivemq.cli.utils.MqttCliShell;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellConnectST {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4"));

    @RegisterExtension
    final MqttCliShell mqttCliShell = new MqttCliShell();

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void whenHelpOptionIsUsed_thenUsageHelpIsDisplayed() throws Exception {
        final List<String> connectCommand = List.of("con", "--help");

        mqttCliShell.executeAsync(connectCommand).awaitStdOut("Usage").awaitStdOut("Options").awaitStdOut("mqtt>");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void whenConnect_thenConnectIsSuccess(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-i",
                "cliTest",
                "-V",
                mqttVersion);

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void whenWrongPortIsUsed_thenConnectIsFailure(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", "22", "-V", mqttVersion, "-i", "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("Connection refused")
                .awaitStdOut("mqtt>")
                .awaitLog("Connection refused");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void whenWrongHostIsUsed_thenConnectIsFailure(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                "unreachable-host",
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                mqttVersion,
                "-i",
                "cliTest");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("nodename nor servname provided, or not known")
                .awaitStdOut("mqtt>")
                .awaitLog("nodename nor servname provided, or not known");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void whenNoClientIdIsUsed_thenConnectIsSuccess(final String mqttVersion) throws Exception {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()), "-V", mqttVersion);


        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        if (mqttVersion.equals("5")) {
            awaitOutput.awaitLog("assignedClientIdentifier=");
        }

    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"5"})
    void whenSessionExpiryIsUsed_thenSessionIsUsed(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                mqttVersion,
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

        mqttCliShell.executeAsync(List.of("dis")).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("sessionTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("sessionExpiryInterval=120")
                .awaitLog("received CONNACK")
                .awaitLog("sessionPresent=true");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3"})
    void whenIdentifierPrefixIsUsed_thenAssignedClientIdStartsWithPrefix(final @NotNull String mqttVersion)
            throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                mqttVersion,
                "-ip",
                "myPrefix");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut("myPrefix")
                .awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"5"})
    void whenUserPropertyIsUsed_thenConnectSentWithUserProperties(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-i",
                "cliTest",
                "-V",
                mqttVersion,
                "-up",
                "key1=value1",
                "-up",
                "key2=value2");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("userProperties=[(key1, value1), (key2, value2)]")
                .awaitLog("received CONNACK");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void whenKeepAliveIsUsed_thenConnectContainsKeepAlive(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-i",
                "cliTest",
                "-V",
                mqttVersion,
                "-k",
                "60");

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("keepAlive=60")
                .awaitLog("received CONNACK");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(strings = {"3", "5"})
    void test_rcvMax(final @NotNull String mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-i",
                "cliTest",
                "-V",
                mqttVersion,
                "--rcvMax",
                "60");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT");

        if (mqttVersion.equals("3")) {
            awaitOutput.awaitStdErr("Restriction receive maximum was set but is unused in MQTT Version MQTT_3_1_1");
        } else {
            awaitOutput.awaitLog("receiveMaximum=60");
        }

        awaitOutput.awaitLog("received CONNACK");

    }
}
