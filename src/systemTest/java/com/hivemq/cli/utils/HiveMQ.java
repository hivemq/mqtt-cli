package com.hivemq.cli.utils;

import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HiveMQ implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private EmbeddedHiveMQ hivemq;
    private List<ConnectPacket> connectPackets;
    private int port;

    @Override
    public void beforeAll(final ExtensionContext context) throws IOException {

        port = generatePort();

        final String hivemqConfig =
                "<hivemq>\n" + "    " +
                "   <listeners>\n" + "        " +
                "       <tcp-listener>\n" +
                "            <port>" + port + "</port>\n" +
                "            <bind-address>0.0.0.0</bind-address>\n" +
                "        </tcp-listener>\n" +
                "    </listeners>\n" +
                "</hivemq>";

        final Path hivemqConfigFolder = Files.createTempDirectory("hivemq-config-folder");
        final File configXml = new File(hivemqConfigFolder.toAbsolutePath().toString(), "config.xml");
        assertTrue(configXml.createNewFile());
        Files.writeString(configXml.toPath(), hivemqConfig);

        final Path hivemqDataFolder = Files.createTempDirectory("hivemq-data-folder");

        connectPackets = new ArrayList<>();
        final EmbeddedExtension embeddedExtension = EmbeddedExtension.builder()
                .withId("test-interceptor-extension")
                .withName("HiveMQ Test Interceptor Extension")
                .withVersion("1.0.0")
                .withPriority(0)
                .withStartPriority(1000)
                .withAuthor("HiveMQ")
                .withExtensionMain(new ExtensionMain() {
                    @Override
                    public void extensionStart(
                            final @NotNull ExtensionStartInput extensionStartInput,
                            final @NotNull ExtensionStartOutput extensionStartOutput) {
                        final ConnectInboundInterceptor connectInboundInterceptor = (connectInboundInput, connectInboundOutput) -> connectPackets.add(connectInboundInput.getConnectPacket());
                        Services.interceptorRegistry().setConnectInboundInterceptorProvider(input -> connectInboundInterceptor);
                    }

                    @Override
                    public void extensionStop(
                            final @NotNull ExtensionStopInput extensionStopInput,
                            final @NotNull ExtensionStopOutput extensionStopOutput) {
                    }
                }).build();

        final EmbeddedHiveMQBuilder builder = EmbeddedHiveMQ.builder()
                .withConfigurationFolder(hivemqConfigFolder)
                .withDataFolder(hivemqDataFolder)
                .withEmbeddedExtension(embeddedExtension);

        try {
            hivemq = builder.build();
            hivemq.start().join();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) {
        hivemq.stop();
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        connectPackets.clear();
    }

    public final @NotNull List<ConnectPacket> getConnectPackets() {
        return connectPackets;
    }

    public int getMqttPort() {
        return port;
    }

    public @NotNull String getHost() {
        return "localhost";
    }

    private int generatePort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        }
    }
}

