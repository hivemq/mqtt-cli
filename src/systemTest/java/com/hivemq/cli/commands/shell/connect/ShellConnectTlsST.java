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
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.broker.TlsConfiguration;
import com.hivemq.cli.utils.broker.TlsVersion;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;

class ShellConnectTlsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder()
            .withTlsConfiguration(TlsConfiguration.builder()
                    .withTlsEnabled(true)
                    .withTlsVersions(TlsVersion.supportedAsList())
                    .build())
            .build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();


    //TRUSTSTORE

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_truststore_tls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--truststore",
                clientTruststore);

        AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);
        awaitOutput.awaitStdOut("Enter truststore password:");
        awaitOutput = mqttCliShell.executeAsync(List.of("clientTruststorePassword"));
        awaitOutput.awaitStdOut(String.format("cliTest@%s", hivemq.getHost()));
        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_password_arguments_truststore_tls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--truststore",
                clientTruststore,
                "--truststore-password",
                "clientTruststorePassword");

        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(connectCommand);
        awaitOutput.awaitStdOut(String.format("cliTest@%s", hivemq.getHost()));
        awaitOutput.awaitLog("sending CONNECT");
        awaitOutput.awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }


    //PEM

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_tls_pem_format(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();

        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey);

        mqttCliShell.executeAsync(connectCommand).awaitLog("sending CONNECT").awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }


    //DER

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_tls_der_format(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();

        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey);

        mqttCliShell.executeAsync(connectCommand).awaitLog("sending CONNECT").awaitLog("received CONNACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }


    //NO CERT

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_tls_no_cert(final char mqttVersion) throws Exception {
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-s");

        mqttCliShell.executeAsync(connectCommand).awaitStdErr("Unable to connect.");
    }
}
