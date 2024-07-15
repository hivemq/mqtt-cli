package com.hivemq.cli.commands.distribution;

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.MqttCli;
import com.hivemq.cli.utils.cli.results.ExecutionResult;
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.broker.assertions.PublishAssertion.assertPublishPacket;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class DockerImageST {

    final @NotNull GenericContainer<?> mqttCli = new GenericContainer<>(OciImages.getImageName("hivemq/mqtt-cli"));

    @RegisterExtension
    @SuppressWarnings("JUnitMalformedDeclaration")
    private final @NotNull HiveMQExtension hivemq = HiveMQExtension.builder().build();

    @AfterEach
    void tearDown() {
        mqttCli.stop();
    }

    @ParameterizedTest
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    @ValueSource(chars = {'3', '5'})
    void test_successfulConnectAndPublish(final char mqttVersion) throws Exception {
        final List<String> publishCommand1 = List.of("pub",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                "cliTest",
                "-t",
                "test",
                "-m",
                "message",
                "-d");
        mqttCli.withCommand(publishCommand1.toArray(new String[0])).start();

        final ExecutionResult executionResult = MqttCli.execute(publishCommand1);

        assertEquals(0, executionResult.getExitCode());
        assertTrue(executionResult.getStandardOutput().contains("sending CONNECT"));
        assertTrue(executionResult.getStandardOutput().contains("received CONNACK"));
        assertTrue(executionResult.getStandardOutput().contains("sending PUBLISH"));
        assertTrue(executionResult.getStandardOutput().contains("finish PUBLISH"));

        assertConnectPacket(hivemq.getConnectPackets().getFirst(),
                connectAssertion -> connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(
                        mqttVersion)));

        assertPublishPacket(hivemq.getPublishPackets().getFirst(), publishAssertion -> {
            publishAssertion.setTopic("test");
            publishAssertion.setPayload(ByteBuffer.wrap("message".getBytes(StandardCharsets.UTF_8)));
        });
    }
}
