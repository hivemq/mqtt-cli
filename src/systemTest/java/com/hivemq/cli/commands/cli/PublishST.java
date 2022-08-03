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

package com.hivemq.cli.commands.cli;

import com.hivemq.cli.utils.CLITestExtension;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishST {

    private static final @NotNull String mqttExec = "build/native/nativeCompile/mqtt-cli";

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4"));

    private final @NotNull CLITestExtension cliTestExtension = new CLITestExtension();

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    void test_successful_publish(@TempDir final @NotNull Path tempDir) throws IOException, InterruptedException {
        final List<String> publishCommand = List.of(mqttExec,
                "pub",
                "-h",
                hivemq.getContainerIpAddress(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-t",
                "test",
                "-m",
                "test");

        final Path inputFile = tempDir.resolve("errorFile.txt");
        assertTrue(inputFile.toFile().createNewFile());

        final Process pub = new ProcessBuilder(publishCommand).redirectError(inputFile.toFile()).start();
        assertEquals(0, pub.waitFor());
        assertEquals(0, Files.readAllBytes(inputFile).length);
    }

    @Test
    void test_publish_missing_topic() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of(mqttExec,
                "pub",
                "-h",
                hivemq.getContainerIpAddress(),
                "-p",
                String.valueOf(hivemq.getMqttPort()));

        final Process pub = new ProcessBuilder(publishCommand).start();

        cliTestExtension.waitForError(pub, "Missing required option: '--topic <topics>'");
        assertEquals(pub.waitFor(), 2);
    }

    @Test
    void test_publish_missing_message() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of(mqttExec,
                "pub",
                "-h",
                hivemq.getContainerIpAddress(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-t",
                "test");

        final Process pub = new ProcessBuilder(publishCommand).start();

        cliTestExtension.waitForError(
                pub,
                "Error: Missing required argument (specify one of these): (-m:file <messageFromFile> | -m <messageFromCommandline>)");
        assertEquals(pub.waitFor(), 2);
    }
}