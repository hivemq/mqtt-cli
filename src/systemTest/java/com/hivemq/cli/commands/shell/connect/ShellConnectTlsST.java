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

import com.google.common.io.Resources;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQ;
import com.hivemq.cli.utils.cli.MqttCliShell;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;

class ShellConnectTlsST {

    @RegisterExtension
    private static final @NotNull HiveMQ HIVE_MQ = HiveMQ.builder().withTlsEnabled(true).build();

    @RegisterExtension
    private final @NotNull MqttCliShell mqttCliShell = new MqttCliShell();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_mutualTls(final char mqttVersion) throws Exception {
        final String clientKeyPem = Resources.getResource("tls/client-key.pem").getPath();
        final String clientCertPem = Resources.getResource("tls/client-cert.pem").getPath();
        final String serverPem = Resources.getResource("tls/server.pem").getPath();

        final List<String> connectCommand = List.of(
                "con",
                "-h",
                HIVE_MQ.getHost(),
                "-p",
                String.valueOf(HIVE_MQ.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "--cafile",
                serverPem,
                "--key",
                clientKeyPem,
                "--cert",
                clientCertPem);

        mqttCliShell.executeAsync(connectCommand).awaitStdOut("Enter private key password:");

        mqttCliShell.executeAsync(List.of("changeme"))
                .awaitStdOut(String.format("cliTest@%s", HIVE_MQ.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("received CONNACK");

        assertConnectPacket(
                HIVE_MQ.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }
}