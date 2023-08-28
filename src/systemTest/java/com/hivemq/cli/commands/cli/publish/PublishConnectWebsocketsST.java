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

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishConnectWebsocketsST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().withWebsocketEnabled(true).build();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_websockets(final char mqttVersion) throws Exception {
        final List<String> publishCommand = List.of(
                "pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getWebsocketsPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-m",
                "message",
                "-ws",
                "-ws:path",
                hivemq.getWebsocketsPath(),
                "-d");

        final ExecutionResult executionResult = MqttCli.execute(publishCommand);
        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));

        assertConnectPacket(
                hivemq.getConnectPackets().get(0),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(hivemq.getPublishPackets().get(0), publishAssertion -> {
            publishAssertion.setTopic("topic");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }
}
