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

import com.hivemq.cli.utils.CLIShellTestExtension;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

public class ConnectST {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4"));

    @RegisterExtension
    private final @NotNull CLIShellTestExtension cliShellTestExtension = new CLIShellTestExtension();

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    void test_successful_connect() {
        cliShellTestExtension.executeCommandWithTimeout(
                "con -h " + hivemq.getContainerIpAddress() + " -p " + hivemq.getMqttPort() + " -i cliTest",
                "cliTest@" + hivemq.getHost() + ">");
    }

    @Test
    void test_unsuccessful_connect() {
        cliShellTestExtension.executeCommandWithErrorWithTimeout("con -h localhost -p 22 -i cliTest", Set.of(
                "Connection refused: localhost/127.0.0.1:22",
                "readAddress(..) failed: Connection reset by peer",
                "Connection reset"));
    }
}
