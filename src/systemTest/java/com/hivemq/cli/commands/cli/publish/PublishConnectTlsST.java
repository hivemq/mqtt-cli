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

import com.google.common.io.Resources;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;

class PublishConnectTlsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension HIVEMQ = HiveMQExtension.builder().withTlsEnabled(true).build();

    @RegisterExtension
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_mutualTls(final char mqttVersion) throws Exception {
        final String clientKeyPem = Resources.getResource("tls/client-key.pem").getPath();
        final String clientCertPem = Resources.getResource("tls/client-cert.pem").getPath();
        final String serverPem = Resources.getResource("tls/server.pem").getPath();

        final List<String> publishCommand = List.of(
                "pub",
                "-h",
                HIVEMQ.getHost(),
                "-p",
                String.valueOf(HIVEMQ.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "test",
                "-m",
                "message",
                "--cafile",
                serverPem,
                "--key",
                clientKeyPem,
                "--cert",
                clientCertPem,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(publishCommand);
        executionResult.awaitStdOut("Enter private key password:");
        executionResult.write("changeme");
        executionResult.awaitStdOut("received PUBLISH acknowledgement");

        assertConnectPacket(
                HIVEMQ.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(HIVEMQ.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }
}
