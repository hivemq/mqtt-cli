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
package com.hivemq.cli.utils;

import com.hivemq.configuration.service.InternalConfigurations;
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
import com.hivemq.migration.meta.PersistenceType;
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
            InternalConfigurations.PAYLOAD_PERSISTENCE_TYPE.set(PersistenceType.FILE);
            InternalConfigurations.RETAINED_MESSAGE_PERSISTENCE_TYPE.set(PersistenceType.FILE);
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
        return "127.0.0.1";
    }

    private int generatePort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        }
    }
}

