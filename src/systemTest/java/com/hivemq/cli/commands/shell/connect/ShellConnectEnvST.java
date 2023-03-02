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

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;

class ShellConnectEnvST {

    private static final @NotNull String PASSWORD_ENV = "PASSWORD";

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliShellExtension
            mqttCliShell = new MqttCliShellExtension(Map.of(PASSWORD_ENV, "password"));

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_passwordEnv(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-pw:env");
        connectCommand.add(PASSWORD_ENV);

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Password-Only Authentication is not allowed in MQTT 3");
            awaitOutput.awaitStdOut("mqtt>");
            awaitOutput.awaitLog("Password-Only Authentication is not allowed in MQTT 3");
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
    void test_userNameAndPasswordEnv(final char mqttVersion) throws Exception {
        final List<String> connectCommand = defaultConnectCommand(mqttVersion);
        connectCommand.add("-u");
        connectCommand.add("user");
        connectCommand.add("-pw:env");
        connectCommand.add(PASSWORD_ENV);

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);

        awaitOutput.awaitStdOut(String.format("@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");
        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("user");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
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
