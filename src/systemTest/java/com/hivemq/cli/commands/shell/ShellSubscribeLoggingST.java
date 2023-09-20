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
import com.hivemq.cli.utils.cli.results.AwaitOutput;
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

public class ShellSubscribeLoggingST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt3_qos0_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "0");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH").awaitLog("MqttPublish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt3_qos0_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "0");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("FAILURE")
                .awaitLog("failed SUBSCRIBE")
                .awaitLog("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt5_qos0_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "0");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH").awaitLog("MqttPublish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt5_qos0_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "0");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("CLI_DENY")
                .awaitStdErr("failed SUBSCRIBE")
                .awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt3_qos1_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "1");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH").awaitLog("MqttPublish");
        //Cannot verify PUBACK as the MQTT-Client does not support MQTT3 interceptors
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt3_qos1_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "1");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("FAILURE")
                .awaitStdErr("failed SUBSCRIBE")
                .awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt5_qos1_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "1");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("sending PUBACK")
                .awaitLog("MqttPubAck");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt5_qos1_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "1");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("CLI_DENY")
                .awaitStdErr("failed SUBSCRIBE")
                .awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt3_qos2_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "2");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH").awaitLog("MqttPublish");
        //Cannot verify PUBREC, PUBREL and PUBCOMP as the MQTT-Client does not support MQTT3 interceptors
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt3_qos2_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '3');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "2");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("FAILURE")
                .awaitStdErr("failed SUBSCRIBE")
                .awaitStdErr("Unable to subscribe");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_with_message_mqtt5_qos2_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "2");
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck");

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

        awaitOutput.awaitLog("received PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("sending PUBREC")
                .awaitLog("MqttPubRec")
                .awaitLog("received PUBREL")
                .awaitLog("MqttPubRel")
                .awaitLog("sending PUBCOMP")
                .awaitLog("MqttPubComp");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_subscribe_mqtt5_qos2_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '5');
        final List<String> subscribeCommand = List.of("sub", "-t", "test", "-q", "2");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending SUBSCRIBE")
                .awaitLog("MqttSubscribe")
                .awaitLog("received SUBACK")
                .awaitLog("MqttSubAck")
                .awaitLog("CLI_DENY")
                .awaitStdErr("failed SUBSCRIBE")
                .awaitStdErr("Unable to subscribe");
    }
}
