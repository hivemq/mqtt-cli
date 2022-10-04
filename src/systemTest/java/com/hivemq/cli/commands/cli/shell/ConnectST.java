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

import com.hivemq.cli.utils.MqttCliShell;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConnectST {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4"));

    @RegisterExtension
    final MqttCliShell mqttCliShell = new MqttCliShell();

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_successful_connect() throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h", hivemq.getHost(),
                "-p", String.valueOf(hivemq.getMqttPort()),
                "-i", "cliTest"
        );

        mqttCliShell.executeAsync(connectCommand).awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
    }


    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_unsuccessful_connect() throws Exception {
        final List<String> connectCommand = List.of(
                "con",
                "-h", "localhost",
                "-p", "22",
                "-i", "cliTest"
        );

        mqttCliShell.executeAsync(connectCommand)
                .awaitStdErr("Unable to connect.")
                .awaitStdOut("mqtt>");
    }

}
