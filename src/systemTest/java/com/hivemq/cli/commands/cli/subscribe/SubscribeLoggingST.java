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

package com.hivemq.cli.commands.cli.subscribe;

import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SubscribeLoggingST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt3_qos0_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt3_qos0_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("FAILURE");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt5_qos0_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.AT_MOST_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt5_qos0_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("CLI_DENY");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt3_qos1_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-q",
                "1",
                "-t",
                "test",
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
        //Cannot verify PUBACK as the MQTT-Client does not support MQTT3 interceptors
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt3_qos1_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-q",
                "1",
                "-t",
                "test",
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("FAILURE");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt5_qos1_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
        executionResult.awaitStdOut("sending PUBACK");
        executionResult.awaitStdOut("MqttPubAck");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt5_qos1_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("CLI_DENY");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt3_qos2_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-q",
                "2",
                "-t",
                "test",
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
        //Cannot verify PUBREC, PUBREL and PUBCOMP as the MQTT-Client does not support MQTT3 interceptors
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt3_qos2_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                "3",
                "-q",
                "2",
                "-t",
                "test",
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("FAILURE");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_with_message_mqtt5_qos2_logging() throws IOException {
        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");

        final Mqtt5BlockingClient publisher = MqttClient.builder()
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .useMqttVersion5()
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("test")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload("message".getBytes(StandardCharsets.UTF_8))
                .send();

        executionResult.awaitStdOut("received PUBLISH");
        executionResult.awaitStdOut("MqttPublish");
        executionResult.awaitStdOut("sending PUBREC");
        executionResult.awaitStdOut("MqttPubRec");
        executionResult.awaitStdOut("received PUBREL");
        executionResult.awaitStdOut("MqttPubRel");
        executionResult.awaitStdOut("sending PUBCOMP");
        executionResult.awaitStdOut("MqttPubComp");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_subscribe_mqtt5_qos2_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        final List<String> subscribeCommand = List.of("sub",
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
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);

        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("MqttConnect");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("MqttConnAck");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("MqttSubscribe");
        executionResult.awaitStdOut("received SUBACK");
        executionResult.awaitStdOut("MqttSubAck");
        executionResult.awaitStdOut("CLI_DENY");
        executionResult.awaitStdErr("failed SUBSCRIBE");
        executionResult.awaitStdErr("Unable to subscribe");
    }
}
