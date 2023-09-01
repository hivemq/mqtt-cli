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

import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

class ShellExitST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_exitContext(final char mqttVersion) throws Exception {
        final List<String> exitCommand = List.of("exit");
        final List<String> lsCommand = List.of("ls");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client");
        mqttCliShell.executeAsync(exitCommand).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(lsCommand).awaitStdOut(String.format("client@%s", hivemq.getHost()));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_exitShell() throws Exception {
        final List<String> exitCommand = List.of("exit");
        mqttCliShell.executeAsync(exitCommand);
        await().until(() -> !mqttCliShell.isAlive());
    }
}
