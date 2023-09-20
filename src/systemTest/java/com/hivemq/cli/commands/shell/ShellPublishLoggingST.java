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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellPublishLoggingST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt3_qos0_logging() throws Exception {
        mqttCliShell.connectClient(hivemq, '3');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "0");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("finish PUBLISH")
                .awaitLog("MqttPublish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt5_qos0_logging() throws Exception {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "0");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("finish PUBLISH")
                .awaitLog("MqttPublishResult");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt5_qos1_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "1");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("received PUBACK")
                .awaitLog("MqttPubAck")
                .awaitLog("finish PUBLISH")
                .awaitLog("MqttQos1Result");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt5_qos1_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '5');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "1");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("received PUBACK")
                .awaitLog("MqttPubAck")
                .awaitLog("CLI_DENY")
                .awaitStdErr("failed PUBLISH")
                .awaitStdErr("Unable to publish");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt5_qos2_logging() throws IOException {
        mqttCliShell.connectClient(hivemq, '5');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "2");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("received PUBREC")
                .awaitLog("MqttPubRec")
                .awaitLog("sending PUBREL")
                .awaitLog("MqttPubRel")
                .awaitLog("received PUBCOMP")
                .awaitLog("MqttPubComp")
                .awaitLog("finish PUBLISH")
                .awaitLog("MqttQos2Result");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_shell_publish_mqtt5_qos2_logging_not_authorized() throws IOException {
        hivemq.setAuthorized(false);

        mqttCliShell.connectClient(hivemq, '5');
        final List<String> publishCommand = List.of("pub", "-t", "test", "-m", "test", "-q", "2");
        mqttCliShell.executeAsync(publishCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending PUBLISH")
                .awaitLog("MqttPublish")
                .awaitLog("received PUBREC")
                .awaitLog("MqttPubRec")
                .awaitLog("CLI_DENY")
                .awaitStdErr("failed PUBLISH")
                .awaitStdErr("Unable to publish");
    }
}
