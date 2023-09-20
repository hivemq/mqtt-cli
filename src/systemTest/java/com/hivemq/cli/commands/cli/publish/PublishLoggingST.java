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

package com.hivemq.cli.commands.cli.publish;

import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublishLoggingST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt3_qos0_logging() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-q",
                "0",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt5_qos0_logging() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "5",
                "-q",
                "0",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublishResult"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt5_qos1_logging() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "5",
                "-q",
                "1",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubAck"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttQos1Result"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt5_qos1_logging_not_authorized() throws IOException, InterruptedException {
        hivemq.setAuthorized(false);

        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "5",
                "-q",
                "1",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubAck"));
        assertTrue(executionResult.getStandardOutput().contains("CLI_DENY"));
        assertTrue(executionResult.getErrorOutput().contains("failed PUBLISH"));
        assertTrue(executionResult.getErrorOutput().contains("Unable to publish"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt5_qos2_logging() throws IOException, InterruptedException {
        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "5",
                "-q",
                "2",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBREC"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubRec"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBREL"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubRel"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBCOMP"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubComp"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttQos2Result"));
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_publish_mqtt5_qos2_logging_not_authorized() throws IOException, InterruptedException {
        hivemq.setAuthorized(false);

        final List<String> publishCommand = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "5",
                "-q",
                "2",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        final ExecutionResult executionResult = MqttCli.execute(publishCommand);

        assertEquals(1, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnect"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("MqttConnAck"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPublish"));
        assertTrue(executionResult.getStandardOutput().contains("received PUBREC"));
        assertTrue(executionResult.getStandardOutput().contains("MqttPubRec"));
        assertTrue(executionResult.getStandardOutput().contains("CLI_DENY"));
        assertTrue(executionResult.getErrorOutput().contains("failed PUBLISH"));
        assertTrue(executionResult.getErrorOutput().contains("Unable to publish"));
    }
}
