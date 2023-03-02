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
import com.hivemq.cli.utils.broker.CipherSuite;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.broker.TlsConfiguration;
import com.hivemq.cli.utils.broker.TlsVersion;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishConnectTlsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder()
            .withTlsConfiguration(TlsConfiguration.builder()
                    .withTlsVersions(List.of(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3))
                    .withCipherSuites(CipherSuite.getAllAsString())
                    .withTlsEnabled(true)
                    .build())
            .build();

    @RegisterExtension
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_tls_pem_format(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();

        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "test",
                "-m",
                "message",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(publishCommand);
        executionResult.awaitStdOut("received PUBLISH acknowledgement");
        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_tls_der_format(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();

        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "test",
                "-m",
                "message",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(publishCommand);
        executionResult.awaitStdOut("received PUBLISH acknowledgement");
        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_tls_no_cert(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "test",
                "-m",
                "message",
                "-s",
                "-d");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Unable to connect"));
    }
}
