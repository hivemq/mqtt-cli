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

import com.hivemq.cli.utils.AwaitOutput;
import com.hivemq.cli.utils.ExecutionResult;
import com.hivemq.cli.utils.MqttCli;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("NewClassNamingConvention")
class SubscribeST {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq-ce"));

    private final @NotNull MqttCli mqttCli = new MqttCli();

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
    void test_successful_subscribe() throws Exception {
        final List<String> subscribeCommand =
                List.of("sub", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()), "-t", "test", "-d");

        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();

        final AwaitOutput awaitOutput = mqttCli.executeAsync(subscribeCommand);

        awaitOutput.awaitStdOut("received SUBACK");

        publisher.publishWith().topic("test").payload("testReturn".getBytes(StandardCharsets.UTF_8)).send();

        awaitOutput.awaitStdOut("testReturn");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_missing_topic() throws Exception {
        final List<String> subscribeCommand =
                List.of("sub", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()));

        final ExecutionResult executionResult = mqttCli.execute(subscribeCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Missing required option: '--topic=<topics>'"));
    }
}
