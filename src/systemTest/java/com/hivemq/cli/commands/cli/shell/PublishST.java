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

public class PublishST {

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
    void test_successful_publish() {
        cliShellTestExtension.executeCommandWithTimeout(
                "con -h " + hivemq.getContainerIpAddress() + " -p " + hivemq.getMqttPort() + " -i cliTest",
                "cliTest@" + hivemq.getHost() + ">");

        cliShellTestExtension.executeCommandWithTimeout("pub -t test -m test", "cliTest@" + hivemq.getHost() + ">");
    }

    @Test
    void test_publish_missing_topic() {
        cliShellTestExtension.executeCommandWithTimeout(
                "con -h " + hivemq.getContainerIpAddress() + " -p " + hivemq.getMqttPort() + " -i cliTest",
                "cliTest@" + hivemq.getHost() + ">");

        cliShellTestExtension.executeCommandWithErrorWithTimeout("pub", "Missing required option: '--topic <topics>'");
    }

    @Test
    void test_publish_missing_message() {
        cliShellTestExtension.executeCommandWithTimeout(
                "con -h " + hivemq.getContainerIpAddress() + " -p " + hivemq.getMqttPort() + " -i cliTest",
                "cliTest@" + hivemq.getHost() + ">");

        cliShellTestExtension.executeCommandWithErrorWithTimeout("pub -t test", Set.of(
                "Error: Missing required argument (specify one of these): (-m <messageFromCommandline> | -m:file <messageFromFile>)",
                "Error: Missing required argument (specify one of these): (-m:file <messageFromFile> | -m <messageFromCommandline>)"));
    }

    @Test
    void test_missing_arguments() {
        cliShellTestExtension.executeCommandWithErrorWithTimeout("pub", "Unmatched argument at index 0: 'pub'");
    }
}
