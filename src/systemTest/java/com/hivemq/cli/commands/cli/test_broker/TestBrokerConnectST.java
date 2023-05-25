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

package com.hivemq.cli.commands.cli.test_broker;

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.TestConnectAssertion.assertTestConnectPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBrokerConnectST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectWrongHost(final char mqttVersion) throws Exception {
        final List<String> testCommand = List.of("test",
                "-h",
                "wrong-host",
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion));

        final ExecutionResult executionResult = MqttCli.execute(testCommand);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Could not connect"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectWrongPort(final char mqttVersion) throws Exception {
        final List<String> testCommand =
                List.of("test", "-h", hivemq.getHost(), "-p", "22", "-V", String.valueOf(mqttVersion));

        final ExecutionResult executionResult = MqttCli.execute(testCommand);
        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Could not connect"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectInvalidTimeOut(final char mqttVersion) throws Exception {
        final List<String> testCommand = List.of("test",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-t",
                "random");

        final ExecutionResult executionResult = MqttCli.execute(testCommand);
        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput()
                .contains("Invalid value for option '--timeOut': 'random' is not an int"));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserName(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-u");
        testCommand.add("username");

        final ExecutionResult executionResult = MqttCli.execute(testCommand);
        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectPassword(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-pw");
        testCommand.add("password");

        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Password-Only Authentication is not allowed in MQTT 3"));
            assertEquals(1, executionResult.getExitCode());
        } else {
            assertTestOutput(executionResult, mqttVersion);
            assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectPasswordEnv(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-pw:env");
        testCommand.add("PASSWORD");

        final ExecutionResult executionResult = MqttCli.execute(testCommand, Map.of("PASSWORD", "password"));

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Password-Only Authentication is not allowed in MQTT 3"));
            assertEquals(1, executionResult.getExitCode());
        } else {
            assertTestOutput(executionResult, mqttVersion);
            assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectPasswordFile(final char mqttVersion) throws Exception {
        final Path passwordFile = Files.createTempFile("password-file", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password");

        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-pw:file");
        testCommand.add(passwordFile.toString());

        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        if (mqttVersion == '3') {
            assertTrue(executionResult.getErrorOutput()
                    .contains("Password-Only Authentication is not allowed in MQTT 3"));
            assertEquals(1, executionResult.getExitCode());
        } else {
            assertTestOutput(executionResult, mqttVersion);
            assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
                connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
                connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
            });
        }
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNameAndPassword(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-u");
        testCommand.add("username");
        testCommand.add("-pw");
        testCommand.add("password");

        final ExecutionResult executionResult = MqttCli.execute(testCommand);
        assertTestOutput(executionResult, mqttVersion);

        assertTestOutput(executionResult, mqttVersion);
        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNameAndPasswordProperties(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        final Map<String, String> properties =
                Map.of("auth.username", "testuser", "auth.password", "testpasswordproperties");
        final ExecutionResult executionResult = MqttCli.execute(testCommand, Map.of(), properties);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("testuser");
            connectAssertion.setPassword(ByteBuffer.wrap("testpasswordproperties".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNameAndPasswordEnv(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-u");
        testCommand.add("username");
        testCommand.add("-pw:env");
        testCommand.add("PASSWORD");

        final ExecutionResult executionResult = MqttCli.execute(testCommand, Map.of("PASSWORD", "password"));

        assertTestOutput(executionResult, mqttVersion);
        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNameAndPasswordEnvProperties(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        final Map<String, String> environments = Map.of("PASSWORD", "testpasswordenvproperties");
        final Map<String, String> properties = Map.of("auth.username", "testuser", "auth.password.env", "PASSWORD");
        final ExecutionResult executionResult = MqttCli.execute(testCommand, environments, properties);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("testuser");
            connectAssertion.setPassword(ByteBuffer.wrap("testpasswordenvproperties".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNamePasswordFile(final char mqttVersion) throws Exception {
        final Path passwordFile = Files.createTempFile("password-file", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "password");

        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-u");
        testCommand.add("username");
        testCommand.add("-pw:file");
        testCommand.add(passwordFile.toString());

        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        assertTestOutput(executionResult, mqttVersion);
        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("username");
            connectAssertion.setPassword(ByteBuffer.wrap("password".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_connectUserNameAndPasswordFileProperties(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        final Path passwordFile = Files.createTempFile("password-file", ".txt");
        passwordFile.toFile().deleteOnExit();
        Files.writeString(passwordFile, "testpasswordfile");
        final Map<String, String> properties =
                Map.of("auth.username", "testuser", "auth.password.file", passwordFile.toString());
        final ExecutionResult executionResult = MqttCli.execute(testCommand, Map.of(), properties);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setUserName("testuser");
            connectAssertion.setPassword(ByteBuffer.wrap("testpasswordfile".getBytes(StandardCharsets.UTF_8)));
        });
    }


    private void assertTestOutput(final @NotNull ExecutionResult executionResult, final char mqttVersion) {
        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("MQTT " + mqttVersion + ": OK"));
    }

    private @NotNull List<String> defaultTestCommand(final char mqttVersion) {
        final ArrayList<String> testCommand = new ArrayList<>();
        testCommand.add("test");
        testCommand.add("-h");
        testCommand.add(hivemq.getHost());
        testCommand.add("-p");
        testCommand.add(String.valueOf(hivemq.getMqttPort()));
        testCommand.add("-V");
        testCommand.add(String.valueOf(mqttVersion));
        return testCommand;
    }
}
