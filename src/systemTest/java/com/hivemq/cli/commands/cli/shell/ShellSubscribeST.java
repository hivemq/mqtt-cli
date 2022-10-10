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

package com.hivemq.cli.commands.cli.shell;

import com.hivemq.cli.utils.HiveMQ;
import com.hivemq.cli.utils.MqttCliShell;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellSubscribeST {

    @RegisterExtension
    private static final @NotNull HiveMQ hivemq = HiveMQ.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliShell mqttCliShell = new MqttCliShell();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successful_subscribe(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub", "-t", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand).awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_subscribe_missing_topic(final char mqttVersion) throws Exception{
        final List<String> subscribeCommand = List.of("sub");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdErr("Missing required option: '--topic <topics>'")
                .awaitStdOut("cliTest@" + hivemq.getHost() + ">");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_missing_arguments() throws Exception {
        final List<String> subscribeCommand = List.of("sub");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut("mqtt>")
                .awaitStdErr("Unmatched argument at index 0: 'sub'");
    }
}
