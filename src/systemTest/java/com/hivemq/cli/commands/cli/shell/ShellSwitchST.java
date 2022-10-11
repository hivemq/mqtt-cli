package com.hivemq.cli.commands.cli.shell;

import com.hivemq.cli.utils.HiveMQ;
import com.hivemq.cli.utils.MqttCliShell;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShellSwitchST {
    @RegisterExtension
    private static final @NotNull HiveMQ hivemq = HiveMQ.builder().build();

    @RegisterExtension
    private final @NotNull MqttCliShell mqttCliShell = new MqttCliShell();

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulSwitchFromContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", String.format("client1@%s", hivemq.getHost()));
        mqttCliShell.connectClient(hivemq, mqttVersion, "client1");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client2");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", hivemq.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulSwitchWithoutContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", String.format("client1@%s", hivemq.getHost()));
        mqttCliShell.connectClient(hivemq, mqttVersion, "client1");
        mqttCliShell.executeAsync(List.of("exit")).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", hivemq.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_hostAndIdentifierWithContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", "-i", "client1", "-h", hivemq.getHost());
        mqttCliShell.connectClient(hivemq, mqttVersion, "client1");
        mqttCliShell.connectClient(hivemq, mqttVersion, "client2");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", hivemq.getHost()));
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_hostAndIdentifierWithoutContext(final char mqttVersion) throws Exception {
        final List<String> switchCommand = List.of("switch", "-i", "client1", "-h", hivemq.getHost());
        mqttCliShell.connectClient(hivemq, mqttVersion, "client1");
        mqttCliShell.executeAsync(List.of("exit")).awaitStdOut("mqtt>");
        mqttCliShell.executeAsync(switchCommand).awaitStdOut(String.format("client1@%s>", hivemq.getHost()));
    }
}
