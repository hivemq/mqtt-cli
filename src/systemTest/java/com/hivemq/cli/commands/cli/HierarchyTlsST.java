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

package com.hivemq.cli.commands.cli;

import com.google.common.io.Resources;
import com.hivemq.cli.utils.MqttVersionConverter;
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
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HierarchyTlsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder()
            .withTlsConfiguration(TlsConfiguration.builder()
                    .withTlsVersions(TlsVersion.supportedAsList())
                    .withClientAuthentication(true)
                    .withTlsEnabled(true)
                    .build())
            .build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();


    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientStoreType,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String clientKeystore = Resources.getResource("tls/client/client-keystore." + clientStoreType).getPath();
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientStoreType).getPath();

        final String certificateAuthorityPublicKey =
                Resources.getResource("tls/hierarchyTest/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/hierarchyTest/client/client-cert.pem").getPath();
        final String clientPrivateKey =
                Resources.getResource("tls/hierarchyTest/client/client-key." + clientKeyType).getPath();

        final String defaultClientKeystore =
                Resources.getResource("tls/hierarchyTest/client/client-keystore." + clientStoreType).getPath();
        final String defaultClientTruststore =
                Resources.getResource("tls/hierarchyTest/client/client-truststore." + clientStoreType).getPath();

        final String defaultCertificateAuthorityPublicKey = Resources.getResource("tls/hierarchyTest/certificateAuthority/ca.pem").getPath();
        final String defaultClientPublicKey = Resources.getResource("tls/hierarchyTest/client/client-cert.pem").getPath();
        final String defaultClientPrivateKey = Resources.getResource("tls/hierarchyTest/client/client-key." + clientKeyType).getPath();

        final Map<String, String> properties = new HashMap<>(Map.of("auth.keystore",
                defaultClientKeystore,
                "auth.keystore.password",
                "clientKeystorePassword",
                "auth.truststore",
                defaultClientTruststore,
                "auth.truststore.password",
                "clientTruststorePassword",
                "auth.client.cert",
                defaultClientPublicKey,
                "auth.client.key",
                defaultClientPrivateKey,
                "auth.server.cafile",
                defaultCertificateAuthorityPublicKey,
                "auth.client.key.password",
                "clientKeyPassword"));
        if (clientStoreType.equals("jks")) {
            properties.put("auth.keystore.privatekey.password", "clientKeyPassword");
        }

        final List<String> publishCommand = new ArrayList<>(List.of("pub",
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
                "--keystore",
                clientKeystore,
                "--truststore",
                clientTruststore,
                "-d",
                "--keystore-password",
                "clientKeystorePassword",
                "--truststore-password",
                "clientTruststorePassword",
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword"));
        if (clientStoreType.equals("jks")) {
            publishCommand.add("--keystore-private-key-password");
            publishCommand.add("clientKeyPassword");
        }

        final ExecutionResult executionResult = MqttCli.execute(publishCommand, Map.of(), properties);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Unable to connect"));
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_private_key_certificate_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientStoreType,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey =
                Resources.getResource("tls/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.pem").getPath();
        final String clientPrivateKey =
                Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final String defaultClientKeystore =
                Resources.getResource("tls/hierarchyTest/client/client-keystore." + clientStoreType).getPath();
        final String defaultClientTruststore =
                Resources.getResource("tls/hierarchyTest/client/client-truststore." + clientStoreType).getPath();

        final String defaultCertificateAuthorityPublicKey = Resources.getResource("tls/hierarchyTest/certificateAuthority/ca.pem").getPath();
        final String defaultClientPublicKey = Resources.getResource("tls/hierarchyTest/client/client-cert.pem").getPath();
        final String defaultClientPrivateKey = Resources.getResource("tls/hierarchyTest/client/client-key." + clientKeyType).getPath();

        final Map<String, String> properties = new HashMap<>(Map.of("auth.keystore",
                defaultClientKeystore,
                "auth.keystore.password",
                "clientKeystorePassword",
                "auth.truststore",
                defaultClientTruststore,
                "auth.truststore.password",
                "clientTruststorePassword",
                "auth.client.cert",
                defaultClientPublicKey,
                "auth.client.key",
                defaultClientPrivateKey,
                "auth.server.cafile",
                defaultCertificateAuthorityPublicKey,
                "auth.client.key.password",
                "clientKeyPassword"));
        if (clientStoreType.equals("jks")) {
            properties.put("auth.keystore.privatekey.password", "clientKeyPassword");
        }

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
                "-d",
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(publishCommand, Map.of(), properties);
        executionResult.awaitStdOut("finish PUBLISH");
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
    void test_default_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientStoreType,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String defaultClientKeystore =
                Resources.getResource("tls/client/client-keystore." + clientStoreType).getPath();
        final String defaultClientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientStoreType).getPath();

        final String defaultCertificateAuthorityPublicKey = Resources.getResource("tls/hierarchyTest/certificateAuthority/ca.pem").getPath();
        final String defaultClientPublicKey = Resources.getResource("tls/hierarchyTest/client/client-cert.pem").getPath();
        final String defaultClientPrivateKey = Resources.getResource("tls/hierarchyTest/client/client-key." + clientKeyType).getPath();

        final Map<String, String> properties = new HashMap<>(Map.of("auth.keystore",
                defaultClientKeystore,
                "auth.keystore.password",
                "clientKeystorePassword",
                "auth.truststore",
                defaultClientTruststore,
                "auth.truststore.password",
                "clientTruststorePassword",
                "auth.client.cert",
                defaultClientPublicKey,
                "auth.client.key",
                defaultClientPrivateKey,
                "auth.server.cafile",
                defaultCertificateAuthorityPublicKey,
                "auth.client.key.password",
                "clientKeyPassword"));
        if (clientStoreType.equals("jks")) {
            properties.put("auth.keystore.privatekey.password", "clientKeyPassword");
        }

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
                "-d");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand, Map.of(), properties);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Unable to connect"));
    }
}
