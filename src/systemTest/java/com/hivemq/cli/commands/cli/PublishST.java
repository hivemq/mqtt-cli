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

import com.hivemq.cli.utils.ExecutionResult;
import com.hivemq.cli.utils.HiveMQ;
import com.hivemq.cli.utils.MqttCli;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishST {

    @RegisterExtension
    private static final @NotNull HiveMQ hivemq = HiveMQ.builder().build();

    private final @NotNull MqttCli mqttCli = new MqttCli();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_successful_publish() throws Exception {
        final List<String> publishCommand = List.of(
                "pub",
                "-h", hivemq.getHost(),
                "-p", String.valueOf(hivemq.getMqttPort()),
                "-t", "test",
                "-m", "test",
                "-d"
        );

        final Mqtt5BlockingClient subscriber = Mqtt5Client.builder()
                .identifier("subscriber")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        subscriber.connect();
        final CountDownLatch receivedPublish = new CountDownLatch(1);
        subscriber.toAsync().subscribeWith().topicFilter("test").callback(ignored -> receivedPublish.countDown()).send();

        final ExecutionResult executionResult = mqttCli.execute(publishCommand);

        assertTrue(receivedPublish.await(10, TimeUnit.SECONDS));
        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBLISH acknowledgement"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_missing_topic() throws Exception {
        final List<String> publishCommand = List.of(
                "pub",
                "-h", hivemq.getHost(),
                "-p", String.valueOf(hivemq.getMqttPort())
        );

        final ExecutionResult executionResult = mqttCli.execute(publishCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Missing required option: '--topic <topics>'"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_missing_message() throws Exception {
        final List<String> publishCommand = List.of(
                "pub",
                "-h", hivemq.getHost(),
                "-p", String.valueOf(hivemq.getMqttPort()),
                "-t", "test"
        );

        final ExecutionResult executionResult = mqttCli.execute(publishCommand);

        assertEquals(2, executionResult.getExitCode());
        assertTrue(executionResult.getErrorOutput().contains("Error: Missing required argument (specify one of these): (-m <messageFromCommandline> | -m:file <messageFromFile>)")
                || executionResult.getErrorOutput().contains("Error: Missing required argument (specify one of these): (-m:file=<messageFromFile> | -m=<messageFromCommandline>)"));
    }
}