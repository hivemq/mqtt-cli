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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.TestConnectAssertion.assertTestConnectPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBrokerST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulConnectAndTest(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_AllTests(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-a");
        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_Timeout(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-t");
        testCommand.add("10");
        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_QosTries(final char mqttVersion) throws Exception {
        final List<String> testCommand = defaultTestCommand(mqttVersion);
        testCommand.add("-q");
        testCommand.add("10");
        final ExecutionResult executionResult = MqttCli.execute(testCommand);

        assertTestOutput(executionResult, mqttVersion);

        assertTestConnectPacket(hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));
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
