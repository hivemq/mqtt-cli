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
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.DisconnectAssertion.assertDisconnectPacket;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShellDisconnectST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulDisconnect(final char mqttVersion) throws Exception {
        final List<String> disconnectCommand = List.of("dis");
        mqttCliShell.connectClient(hivemq, mqttVersion, "myClient");
        mqttCliShell.executeAsync(disconnectCommand).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");
        assertDisconnectPacket(
                hivemq.getDisconnectInformation().get(0),
                disconnectAssertion -> disconnectAssertion.setDisconnectedClient("myClient"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_sessionExpiryInterval(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-se",
                "120");
        final List<String> disconnectCommand = List.of("dis", "-e", "60");
        mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        mqttCliShell.executeAsync(disconnectCommand).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");
        assertDisconnectPacket(hivemq.getDisconnectInformation().get(0), disconnectAssertion -> {
            if (mqttVersion == '5') {
                disconnectAssertion.setSessionExpiryInterval(60);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_reasonString(final char mqttVersion) throws Exception {
        final List<String> disconnectCommand = List.of("dis", "-r", "test-reason");

        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(disconnectCommand).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Reason string was set but is unused in Mqtt version MQTT_3_1_1");
        }

        assertDisconnectPacket(hivemq.getDisconnectInformation().get(0), disconnectAssertion -> {
            if (mqttVersion == '5') {
                disconnectAssertion.setReasonString("test-reason");
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> disconnectCommand = List.of("dis", "-up", "key1=value1", "-up", "key2=value2");

        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput =
                mqttCliShell.executeAsync(disconnectCommand).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("User properties were set but are unused in Mqtt version MQTT_3_1_1");
        }

        assertDisconnectPacket(hivemq.getDisconnectInformation().get(0), disconnectAssertion -> {
            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties =
                        UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder()
                                .add(MqttUserProperty.of("key1", "value1"))
                                .add(MqttUserProperty.of("key2", "value2"))
                                .build());
                disconnectAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_disconnectById(final char mqttVersion) throws Exception {
        final String clientId = "myTestClient";
        final List<String> disconnectCommand = List.of("dis", "-i", clientId);

        mqttCliShell.connectClient(hivemq, mqttVersion, clientId);
        mqttCliShell.executeAsync(List.of("exit")).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(disconnectCommand).awaitStdOut("mqtt>").awaitLog("sending DISCONNECT");

        assertDisconnectPacket(
                hivemq.getDisconnectInformation().get(0),
                disconnectAssertion -> disconnectAssertion.setDisconnectedClient(clientId));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_disconnectAll(final char mqttVersion) throws Exception {
        final List<String> disconnectAllCommand = List.of("dis", "-a");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client1");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client2");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client3");

        mqttCliShell.executeAsync(disconnectAllCommand)
                .awaitStdOut("mqtt>")
                .awaitLog("sending DISCONNECT")
                .awaitLog("sending DISCONNECT")
                .awaitLog("sending DISCONNECT");

        final String clientId1 = hivemq.getDisconnectInformation().get(0).getClientId();
        final String clientId2 = hivemq.getDisconnectInformation().get(1).getClientId();
        final String clientId3 = hivemq.getDisconnectInformation().get(2).getClientId();
        final ArrayList<String> clientIdPool = new ArrayList<>();
        clientIdPool.add(clientId1);
        clientIdPool.add(clientId2);
        clientIdPool.add(clientId3);

        assertTrue(clientIdPool.containsAll(List.of("client1", "client2", "client3")));

        assertDisconnectPacket(
                hivemq.getDisconnectInformation().get(0),
                disconnectAssertion -> disconnectAssertion.setDisconnectedClient(clientId1));

        assertDisconnectPacket(
                hivemq.getDisconnectInformation().get(1),
                disconnectAssertion -> disconnectAssertion.setDisconnectedClient(clientId2));

        assertDisconnectPacket(
                hivemq.getDisconnectInformation().get(2),
                disconnectAssertion -> disconnectAssertion.setDisconnectedClient(clientId3));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_notConnectedDisconnect() throws Exception {
        final List<String> disconnectCommand = List.of("dis");
        mqttCliShell.executeAsync(disconnectCommand)
                .awaitStdErr("Missing required option '--identifier=<identifier>'")
                .awaitStdOut("mqtt>");
    }
}
