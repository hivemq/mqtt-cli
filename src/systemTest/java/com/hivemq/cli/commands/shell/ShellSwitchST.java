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

package com.hivemq.cli.commands.shell;

import com.hivemq.cli.utils.broker.HiveMQ;
import com.hivemq.cli.utils.cli.MqttCliShell;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

class ShellSwitchST {

    @RegisterExtension
    private static final @NotNull HiveMQ HIVE_MQ = HiveMQ.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliShell mqttCliShell = new MqttCliShell();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulSwitchFromContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", String.format("client1@%s", HIVE_MQ.getHost()));
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client2");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", HIVE_MQ.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulSwitchWithoutContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", String.format("client1@%s", HIVE_MQ.getHost()));
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.executeAsync(List.of("exit")).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", HIVE_MQ.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_hostAndIdentifierWithContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", "-i", "client1", "-h", HIVE_MQ.getHost());
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client2");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", HIVE_MQ.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_hostAndIdentifierWithoutContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", "-i", "client1", "-h", HIVE_MQ.getHost());
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.executeAsync(List.of("exit")).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", HIVE_MQ.getHost()));
    }
}
