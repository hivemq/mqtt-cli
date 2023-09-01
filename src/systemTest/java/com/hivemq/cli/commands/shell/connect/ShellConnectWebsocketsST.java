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
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;

class ShellConnectWebsocketsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().withWebsocketEnabled(true).build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_defaultConnect(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getWebsocketsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-ws",
                "-ws:path",
                hivemq.getWebsocketsPath());

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        final ConnectPacket connectPacket = hivemq.getConnectPackets().get(0);
        assertConnectPacket(connectPacket,
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }
}
