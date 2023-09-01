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

import com.google.common.collect.ImmutableList;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliShellExtension;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import com.hivemq.extensions.packets.general.UserPropertiesImpl;
import com.hivemq.mqtt.message.mqtt5.MqttUserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.UnsubscribeAssertion.assertUnsubscribePacket;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellUnsubscribeST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull MqttCliShellExtension mqttCliShell = new MqttCliShellExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulUnsubscribe(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("unsub", "-t", "test");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending UNSUBSCRIBE")
                .awaitLog("received UNSUBACK");

        assertUnsubscribePacket(
                hivemq.getUnsubscribePackets().get(0),
                unsubscribeAssertion -> unsubscribeAssertion.setTopicFilters(List.of("test")));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_multipleTopics(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("unsub", "-t", "test1", "-t", "test2", "-t", "test3");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitLog("sending UNSUBSCRIBE")
                .awaitLog("received UNSUBACK")
                .awaitLog("sending UNSUBSCRIBE")
                .awaitLog("received UNSUBACK")
                .awaitLog("sending UNSUBSCRIBE")
                .awaitLog("received UNSUBACK");


        assertUnsubscribePacket(
                hivemq.getUnsubscribePackets().get(0),
                unsubscribeAssertion -> unsubscribeAssertion.setTopicFilters(List.of("test1")));

        assertUnsubscribePacket(
                hivemq.getUnsubscribePackets().get(1),
                unsubscribeAssertion -> unsubscribeAssertion.setTopicFilters(List.of("test2")));

        assertUnsubscribePacket(
                hivemq.getUnsubscribePackets().get(2),
                unsubscribeAssertion -> unsubscribeAssertion.setTopicFilters(List.of("test3")));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_userProperties(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand =
                List.of("unsub", "-t", "test", "-up", "key1=value1", "-up", "key2=value2");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        final AwaitOutput awaitOutput = mqttCliShell.executeAsync(subscribeCommand);

        if (mqttVersion == '3') {
            awaitOutput.awaitStdErr("Unsubscribe user properties were set but are unused in MQTT Version MQTT_3_1_1");
            awaitOutput.awaitLog("Unsubscribe user properties were set but are unused in MQTT Version MQTT_3_1_1");
        }
        awaitOutput.awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("sending UNSUBSCRIBE");
        awaitOutput.awaitLog("received UNSUBACK");

        assertUnsubscribePacket(hivemq.getUnsubscribePackets().get(0), unsubscribeAssertion -> {
            unsubscribeAssertion.setTopicFilters(List.of("test"));

            if (mqttVersion == '5') {
                final UserPropertiesImpl expectedUserProperties =
                        UserPropertiesImpl.of(ImmutableList.<MqttUserProperty>builder()
                                .add(MqttUserProperty.of("key1", "value1"))
                                .add(MqttUserProperty.of("key2", "value2"))
                                .build());
                unsubscribeAssertion.setUserProperties(expectedUserProperties);
            }
        });
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_missingTopic(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("unsub");
        mqttCliShell.connectClient(hivemq, mqttVersion);
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()))
                .awaitStdErr("Missing required option: '--topic <topics>'")
                .awaitStdErr("Try 'help unsub' for more information.");

        assertEquals(0, hivemq.getUnsubscribePackets().size());
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void test_UnsubscribeWhileNotConnected() throws Exception {
        final List<String> subscribeCommand = List.of("unsub");
        mqttCliShell.executeAsync(subscribeCommand)
                .awaitStdErr("Unmatched argument at index 0: 'unsub'")
                .awaitStdErr("Try 'help' to get a list of commands.");

        assertEquals(0, hivemq.getUnsubscribePackets().size());
    }
}
