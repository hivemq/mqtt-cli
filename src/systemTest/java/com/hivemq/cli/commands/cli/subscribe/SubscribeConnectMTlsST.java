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

package com.hivemq.cli.commands.cli.subscribe;

import com.google.common.io.Resources;
import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.broker.TlsConfiguration;
import com.hivemq.cli.utils.broker.TlsVersion;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.subscribe.RetainHandling;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import com.hivemq.extensions.packets.subscribe.SubscriptionImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;

class SubscribeConnectMTlsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder()
            .withTlsConfiguration(TlsConfiguration.builder()
                    .withTlsEnabled(true)
                    .withTlsVersions(TlsVersion.supportedAsList())
                    .withClientAuthentication(true)
                    .build())
            .build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();


    //KEYSTORE / TRUSTSTORE

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_similar_private_key_password_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientKeystore =
                Resources.getResource("tls/client/client-keystore.similar_private_key_password." + clientKeyType)
                        .getPath();
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--keystore",
                clientKeystore,
                "--truststore",
                clientTruststore,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("Enter keystore password:");
        executionResult.write("clientKeystorePassword");
        executionResult.awaitStdOut("Enter truststore password:");
        executionResult.write("clientTruststorePassword");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientKeystore = Resources.getResource("tls/client/client-keystore." + clientKeyType).getPath();
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--keystore",
                clientKeystore,
                "--truststore",
                clientTruststore,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("Enter keystore password:");
        executionResult.write("clientKeystorePassword");
        if (clientKeyType.equals("jks")) {
            executionResult.awaitStdOut("Enter keystore private key password:");
            executionResult.write("clientKeyPassword");
        }
        executionResult.awaitStdOut("Enter truststore password:");
        executionResult.write("clientTruststorePassword");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_password_arguments_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientKeystore = Resources.getResource("tls/client/client-keystore." + clientKeyType).getPath();
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final List<String> subscribeCommand = new ArrayList<>(List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
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
                "clientTruststorePassword"));
        if (clientKeyType.equals("jks")) {
            subscribeCommand.add("--keystore-private-key-password");
            subscribeCommand.add("clientKeyPassword");
        }

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_properties_keystore_truststore_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "jks", "p12"}) final @NotNull String clientKeyType) throws Exception {
        final String clientKeystore = Resources.getResource("tls/client/client-keystore." + clientKeyType).getPath();
        final String clientTruststore =
                Resources.getResource("tls/client/client-truststore." + clientKeyType).getPath();

        final Map<String, String> properties = new HashMap<>(Map.of("auth.keystore",
                clientKeystore,
                "auth.keystore.password",
                "clientKeystorePassword",
                "auth.truststore",
                clientTruststore,
                "auth.truststore.password",
                "clientTruststorePassword"));
        if (clientKeyType.equals("jks")) {
            properties.put("auth.keystore.privatekey.password", "clientKeyPassword");
        }

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand, Map.of(), properties);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }


    //PEM

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_unencrypted_pem_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.unencrypted.pem", "pkcs8.unencrypted.pem"}) final @NotNull String clientKeyType)
            throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.pem").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_encrypted_pem_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.pem").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("Enter private key password:");
        executionResult.write("clientKeyPassword");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_password_options_encrypted_pem_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.pem").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword",
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_properties_encrypted_pem_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.pem",
                    //"pkcs1.camellia256.pem",
                    //"pkcs1.des.pem",
                    "pkcs1.des3.pem", "pkcs8.aes256.pem",
                    //"pkcs8.camellia256.pem",
                    //"pkcs8.des.pem",
                    "pkcs8.des3.pem"}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.pem").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.pem").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final Map<String, String> properties = Map.of("auth.client.cert",
                clientPublicKey,
                "auth.client.key",
                clientPrivateKey,
                "auth.server.cafile",
                certificateAuthorityPublicKey,
                "auth.client.key.password",
                "clientKeyPassword");

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword",
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand, Map.of(), properties);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }


    //DER

    @Disabled("Not yet supported")
    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_unencrypted_der_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.unencrypted.der", "pkcs8.unencrypted.der"}) final @NotNull String clientKeyType)
            throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.cer").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @Disabled("Not yet supported")
    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_encrypted_der_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.der",
                    "pkcs1.camellia256.der",
                    "pkcs1.des.der",
                    "pkcs1.des3.der",
                    "pkcs8.aes256.der",
                    "pkcs8.camellia256.der",
                    "pkcs8.des.der",
                    "pkcs8.des3.der",}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.cer").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("Enter private key password:");
        executionResult.write("clientKeyPassword");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @Disabled("Not yet supported")
    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_password_options_encrypted_der_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.der",
                    "pkcs1.camellia256.der",
                    "pkcs1.des.der",
                    "pkcs1.des3.der",
                    "pkcs8.aes256.der",
                    "pkcs8.camellia256.der",
                    "pkcs8.des.der",
                    "pkcs8.des3.der",}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.cer").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword",
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }

    @Disabled("Not yet supported")
    @CartesianTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_properties_encrypted_der_private_keys_mutualTls(
            @CartesianTest.Values(chars = {'3', '5'}) final char mqttVersion,
            @CartesianTest.Enum final @NotNull TlsVersion tlsVersion,
            @CartesianTest.Values(strings = {
                    "pkcs1.aes256.der",
                    "pkcs1.camellia256.der",
                    "pkcs1.des.der",
                    "pkcs1.des3.der",
                    "pkcs8.aes256.der",
                    "pkcs8.camellia256.der",
                    "pkcs8.des.der",
                    "pkcs8.des3.der",}) final @NotNull String clientKeyType) throws Exception {
        final String certificateAuthorityPublicKey = Resources.getResource("tls/certificateAuthority/ca.cer").getPath();
        final String clientPublicKey = Resources.getResource("tls/client/client-cert.cer").getPath();
        final String clientPrivateKey = Resources.getResource("tls/client/client-key." + clientKeyType).getPath();

        final Map<String, String> properties = Map.of("auth.client.cert",
                clientPublicKey,
                "auth.client.key",
                clientPrivateKey,
                "auth.server.cafile",
                certificateAuthorityPublicKey,
                "auth.client.key.password",
                "clientKeyPassword");

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttTlsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-s",
                "--tls-version",
                tlsVersion.toString(),
                "--cafile",
                certificateAuthorityPublicKey,
                "--key",
                clientPrivateKey,
                "--cert",
                clientPublicKey,
                "--keypw",
                "clientKeyPassword",
                "-d");

        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand, Map.of(), properties);
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.EXACTLY_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });
    }
}
