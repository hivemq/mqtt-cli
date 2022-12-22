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

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQ;
import com.hivemq.cli.utils.cli.MqttCliShell;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

class ShellListST {

    @RegisterExtension
    private static final @NotNull HiveMQ HIVE_MQ = HiveMQ.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliShell mqttCliShell = new MqttCliShell();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_emptyList() throws Exception {
        final List<String> listCommand = List.of("ls");
        mqttCliShell.executeAsync(listCommand).awaitStdOut("mqtt>");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_listConnectedClients(final char mqttVersion) throws Exception {
        final List<String> listCommand = List.of("ls");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client2");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client3");
        mqttCliShell.executeAsync(listCommand)
                .awaitStdOut(String.format("client1@%s", HIVE_MQ.getHost()))
                .awaitStdOut(String.format("client2@%s", HIVE_MQ.getHost()))
                .awaitStdOut(String.format("client3@%s", HIVE_MQ.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_listConnectedClientsReverse(final char mqttVersion) throws Exception {
        final List<String> listCommand = List.of("ls", "-r");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client2");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client3");
        mqttCliShell.executeAsync(listCommand)
                .awaitStdOut(String.format("client3@%s", HIVE_MQ.getHost()))
                .awaitStdOut(String.format("client2@%s", HIVE_MQ.getHost()))
                .awaitStdOut(String.format("client1@%s", HIVE_MQ.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_listLongConnectedClients(final char mqttVersion) throws Exception {
        final List<String> listCommand = List.of("ls", "-l");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client1");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client2");
        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "client3");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(listCommand);

        awaitOutput.awaitStdOut("CONNECTED")
                .awaitStdOut("client1")
                .awaitStdOut(HIVE_MQ.getHost())
                .awaitStdOut(String.valueOf(HIVE_MQ.getMqttPort()))
                .awaitStdOut(MqttVersionConverter.toClientVersion(mqttVersion).name())
                .awaitStdOut("NO_SSL");

        awaitOutput.awaitStdOut("CONNECTED")
                .awaitStdOut("client2")
                .awaitStdOut(HIVE_MQ.getHost())
                .awaitStdOut(String.valueOf(HIVE_MQ.getMqttPort()))
                .awaitStdOut(MqttVersionConverter.toClientVersion(mqttVersion).name())
                .awaitStdOut("NO_SSL");

        awaitOutput.awaitStdOut("CONNECTED")
                .awaitStdOut("client3")
                .awaitStdOut(HIVE_MQ.getHost())
                .awaitStdOut(String.valueOf(HIVE_MQ.getMqttPort()))
                .awaitStdOut(MqttVersionConverter.toClientVersion(mqttVersion).name())
                .awaitStdOut("NO_SSL");
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_listSubscriptions(final char mqttVersion) throws Exception {
        final List<String> listCommand = List.of("ls", "-s");

        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "subscription-client1");
        mqttCliShell.executeAsync(List.of("sub", "-t", "topic1"))
                .awaitStdOut(String.format("subscription-client1@%s>", HIVE_MQ.getHost()))
                .awaitLog("received SUBACK");

        mqttCliShell.connectClient(HIVE_MQ, mqttVersion, "subscription-client2");
        mqttCliShell.executeAsync(List.of("sub", "-t", "topic2"))
                .awaitStdOut(String.format("subscription-client2@%s>", HIVE_MQ.getHost()))
                .awaitLog("received SUBACK");

        mqttCliShell.executeAsync(listCommand)
                .awaitStdOut(String.format("subscription-client1@%s", HIVE_MQ.getHost()))
                .awaitStdOut("-subscribed topics: [topic1]")
                .awaitStdOut(String.format("subscription-client2@%s", HIVE_MQ.getHost()))
                .awaitStdOut("-subscribed topics: [topic2]");
    }

}