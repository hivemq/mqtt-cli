package com.hivemq.cli.commands.cli.subscribe;

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCliAsyncExtension;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.subscribe.RetainHandling;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import com.hivemq.extensions.packets.subscribe.SubscriptionImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.SubscribeAssertion.assertSubscribePacket;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SubscribeWithSessionST {

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliAsyncExtension mqttCli = new MqttCliAsyncExtension();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulMessageReceiveAfterSessionResume(final char mqttVersion) throws Exception {
        final List<String> subscribeCommand = List.of("sub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "topic",
                "-q",
                "1",
                "--no-cleanStart",
                "-se",
                "300",
                "-d");
        final ExecutionResultAsync executionResult = mqttCli.executeAsync(subscribeCommand);
        executionResult.awaitStdOut("sending CONNECT");
        executionResult.awaitStdOut("received CONNACK");
        executionResult.awaitStdOut("sending SUBSCRIBE");
        executionResult.awaitStdOut("received SUBACK");

        publishMessage("message");

        executionResult.awaitStdOut("message");

        assertConnectPacket(hivemq.getConnectPackets().get(0), connectAssertion -> {
            connectAssertion.setCleanStart(false);
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(4294967295L);
            } else {
                connectAssertion.setSessionExpiryInterval(300L);
            }
        });
        assertSubscribePacket(hivemq.getSubscribePackets().get(0), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.AT_LEAST_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });

        mqttCli.killProcesses();

        publishMessage("message1");

        final ExecutionResultAsync executionResultAfterReconnect = mqttCli.executeAsync(subscribeCommand);
        executionResultAfterReconnect.awaitStdOut("sending CONNECT");
        executionResultAfterReconnect.awaitStdOut("received CONNACK");
        executionResultAfterReconnect.awaitStdOut("message1");
        executionResultAfterReconnect.awaitStdOut("sending SUBSCRIBE");
        executionResultAfterReconnect.awaitStdOut("received SUBACK");

        assertConnectPacket(hivemq.getConnectPackets().get(3), connectAssertion -> {
            connectAssertion.setCleanStart(false);
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            if (mqttVersion == '3') {
                connectAssertion.setSessionExpiryInterval(4294967295L);
            } else {
                connectAssertion.setSessionExpiryInterval(300L);
            }
        });
        assertSubscribePacket(hivemq.getSubscribePackets().get(1), subscribeAssertion -> {
            final List<Subscription> expectedSubscriptions =
                    List.of(new SubscriptionImpl("topic", Qos.AT_LEAST_ONCE, RetainHandling.SEND, false, false));
            subscribeAssertion.setSubscriptions(expectedSubscriptions);
        });


        assertFalse(executionResultAfterReconnect.stdErrContains("Exception"));
        assertFalse(executionResultAfterReconnect.stdErrContains(
                "A publish must not be acknowledged if manual acknowledgement is not enabled"));
    }

    private void publishMessage(final @NotNull String message) {
        final Mqtt5BlockingClient publisher = Mqtt5Client.builder()
                .identifier("publisher")
                .serverHost(hivemq.getHost())
                .serverPort(hivemq.getMqttPort())
                .buildBlocking();
        publisher.connect();
        publisher.publishWith()
                .topic("topic")
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.EXACTLY_ONCE)
                .send();
    }
}
