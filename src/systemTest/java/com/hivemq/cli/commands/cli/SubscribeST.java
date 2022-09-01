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
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubscribeST {

    private static final @NotNull String mqttExec = "build/native/nativeCompile/mqtt-cli";
    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq-ce"));

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
    void test_successful_subscribe() throws Exception {
        final List<String> publishCommand = List.of(mqttExec,
                "sub",
                "-h",
                hivemq.getContainerIpAddress(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-t",
                "test",
                "-d");

        final Process sub = new ProcessBuilder(publishCommand).start();

        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getContainerIpAddress())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();

        cliTestExtension.waitForOutputWithTimeout(sub, "received SUBACK");
        final CompletableFuture<Void> testReturn = cliTestExtension.waitForOutput(sub, "testReturn");
        publisher.publishWith().topic("test").payload("testReturn".getBytes(StandardCharsets.UTF_8)).send();
        testReturn.get(3, TimeUnit.SECONDS);
    }

    @Test
    void test_subscribe_missing_topic() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of(mqttExec,
                "sub",
                "-h",
                hivemq.getContainerIpAddress(),
                "-p",
                String.valueOf(hivemq.getMqttPort()));
        final Process sub = new ProcessBuilder(publishCommand).start();

        cliTestExtension.waitForError(sub, "Missing required option: '--topic <topics>'");
        assertEquals(sub.waitFor(), 2);
    }
}
