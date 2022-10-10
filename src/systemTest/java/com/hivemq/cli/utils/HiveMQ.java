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

import com.google.common.io.Resources;
import com.hivemq.cli.utils.assertions.DisconnectInformation;
import com.hivemq.configuration.service.InternalConfigurations;
import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
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

    private static final String WEBSOCKETS_PATH = "/mqtt-custom";
    private static final String BIND_ADDRESS = "localhost";

    private EmbeddedHiveMQ hivemq;

    private List<ConnectPacket> connectPackets;
    private List<PublishPacket> publishPackets;
    private List<SubscribePacket> subscribePackets;
    private List<DisconnectInformation> disconnectInformations;

    private int port = -1;
    private int tlsPort = -1;
    private int websocketsPort = -1;

    private final boolean tlsEnabled;
    private final boolean websocketEnabled;

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public HiveMQ(final boolean tlsEnabled, final boolean websocketEnabled) {
        this.tlsEnabled = tlsEnabled;
        this.websocketEnabled = websocketEnabled;
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws IOException {

        this.port = generatePort();
        this.connectPackets = new ArrayList<>();
        this.publishPackets = new ArrayList<>();
        this.disconnectInformations = new ArrayList<>();
        this.subscribePackets = new ArrayList<>();

        final String tlsConfig = setupTls();
        final String websocketsConfig = setupWebsockets();

        final String hivemqConfig =
                "<hivemq>\n" + "    " +
                "   <listeners>\n" + "        " +
                "       <tcp-listener>\n" +
                "            <port>" + port + "</port>\n" +
                "            <bind-address>" + BIND_ADDRESS + "</bind-address>\n" +
                "        </tcp-listener>\n" +
                        tlsConfig +
                        websocketsConfig +
                "    </listeners>\n" +
                "</hivemq>";

        final Path hivemqConfigFolder = Files.createTempDirectory("hivemq-config-folder");
        hivemqConfigFolder.toFile().deleteOnExit();
        final File configXml = new File(hivemqConfigFolder.toAbsolutePath().toString(), "config.xml");
        assertTrue(configXml.createNewFile());
        Files.writeString(configXml.toPath(), hivemqConfig);

        final Path hivemqDataFolder = Files.createTempDirectory("hivemq-data-folder");
        hivemqDataFolder.toFile().deleteOnExit();

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
                        Services.initializerRegistry().setClientInitializer((initializerInput, clientContext) -> {

                            clientContext.addDisconnectInboundInterceptor((disconnectInboundInput, disconnectInboundOutput) -> {
                                disconnectInformations.add(new DisconnectInformation(disconnectInboundInput.getDisconnectPacket(), disconnectInboundInput.getClientInformation().getClientId()));
                            });

                            clientContext.addPublishInboundInterceptor((publishInboundInput, publishInboundOutput) -> {
                                publishPackets.add(publishInboundInput.getPublishPacket());
                            });

                            clientContext.addSubscribeInboundInterceptor((subscribeInboundInput, subscribeInboundOutput) -> {
                                subscribePackets.add(subscribeInboundInput.getSubscribePacket());
                            });
                        });
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
        disconnectInformations.clear();
        publishPackets.clear();
    }

    public @NotNull List<ConnectPacket> getConnectPackets() {
        return connectPackets;
    }

    public @NotNull List<PublishPacket> getPublishPackets() {
        return publishPackets;
    }

    public @NotNull List<SubscribePacket> getSubscribePackets() {
        return subscribePackets;
    }

    public @NotNull List<DisconnectInformation> getDisconnectInformations() {
        return disconnectInformations;
    }

    public int getMqttPort() {
        return port;
    }

    public int getMqttTlsPort() {
        if (tlsPort == -1) {
            throw new RuntimeException("HiveMQ was initialized without a TLS listener.");
        }
        return tlsPort;
    }

    public int getWebsocketsPort() {
        if (websocketsPort == -1) {
            throw new RuntimeException("HiveMQ was initialized without a websocket listener.");
        }
        return websocketsPort;
    }

    public @NotNull String getWebsocketsPath() {
        return WEBSOCKETS_PATH;
    }

    public @NotNull String getHost() {
        return BIND_ADDRESS;
    }

    private int generatePort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private @NotNull String setupTls() throws IOException {
        String tlsConfig = "";
        if (tlsEnabled) {
            this.tlsPort = generatePort();
            final String brokerKeyStorePath = Resources.getResource("tls/broker-keystore.jks").getPath();
            final String brokerTrustStorePath = Resources.getResource("tls/client-keystore.jks").getPath();
            tlsConfig =
                    "<tls-tcp-listener>\n" +
                    "           <port>" + tlsPort + "</port>\n" +
                    "           <bind-address>" + BIND_ADDRESS + "</bind-address>\n" +
                    "           <tls>\n" +
                    "                <keystore>" +
                    "                   <path>" + brokerKeyStorePath +  "</path>\n" +
                    "                   <password>changeme</password>\n" +
                    "                   <private-key-password>changeme</private-key-password>\n" +
                    "                </keystore>\n" +
                    "                <client-authentication-mode>REQUIRED</client-authentication-mode>\n" +
                    "                <truststore>\n" +
                    "                   <path>" + brokerTrustStorePath + "</path>\n" +
                    "                   <password>changeme</password>\n" +
                    "                </truststore>\n" +
                    "           </tls>\n" +
                    "</tls-tcp-listener>\n";
        }
        return tlsConfig;
    }

    private @NotNull String setupWebsockets() throws IOException {
        String websocketsConfig = "";
        if (websocketEnabled) {
            this.websocketsPort = generatePort();
            websocketsConfig =
            "<websocket-listener>\n" +
            "          <port>" + websocketsPort + "</port>\n" +
            "          <bind-address>" + BIND_ADDRESS + "</bind-address>\n" +
            "          <path>" + WEBSOCKETS_PATH + "</path>\n" +
            "          <name>my-websocket-listener</name>\n" +
            "          <subprotocols>\n" +
            "              <subprotocol>mqttv3.1</subprotocol>\n" +
            "              <subprotocol>mqtt</subprotocol>\n" +
            "          </subprotocols>\n" +
            "          <allow-extensions>true</allow-extensions>\n" +
            "</websocket-listener>";
        }
        return websocketsConfig;
    }

    public static class Builder {
        private boolean tlsEnabled = false;
        private boolean websocketEnabled = false;

        private Builder() {
        }

        public @NotNull HiveMQ build() {
            return new HiveMQ(tlsEnabled, websocketEnabled);
        }

        public @NotNull Builder withTlsEnabled(final boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
            return this;
        }

        public @NotNull Builder withWebsocketEnabled(final boolean websocketEnabled) {
            this.websocketEnabled = websocketEnabled;
            return this;
        }
    }
}

