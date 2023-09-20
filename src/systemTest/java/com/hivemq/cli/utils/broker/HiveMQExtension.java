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

package com.hivemq.cli.utils.broker;

import com.google.common.io.Resources;
import com.hivemq.cli.utils.broker.assertions.DisconnectInformation;
import com.hivemq.configuration.service.InternalConfigurations;
import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.auth.PublishAuthorizer;
import com.hivemq.extension.sdk.api.auth.SubscriptionAuthorizer;
import com.hivemq.extension.sdk.api.auth.parameter.PublishAuthorizerInput;
import com.hivemq.extension.sdk.api.auth.parameter.PublishAuthorizerOutput;
import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerInput;
import com.hivemq.extension.sdk.api.auth.parameter.SubscriptionAuthorizerOutput;
import com.hivemq.extension.sdk.api.interceptor.connack.ConnackOutboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.packets.connack.ConnackPacket;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.packets.publish.AckReasonCode;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extension.sdk.api.packets.subscribe.SubackReasonCode;
import com.hivemq.extension.sdk.api.packets.subscribe.SubscribePacket;
import com.hivemq.extension.sdk.api.packets.unsubscribe.UnsubscribePacket;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.migration.meta.PersistenceType;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HiveMQExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private static final @NotNull String WEBSOCKETS_PATH = "/mqtt-custom";
    private static final @NotNull String BIND_ADDRESS = "localhost";

    private @Nullable EmbeddedHiveMQ hivemq;

    private @Nullable Path hivemqConfigFolder;
    private @Nullable Path hivemqDataFolder;

    private @Nullable List<ConnectPacket> connectPackets;
    private @Nullable List<ConnackPacket> connackPackets;
    private @Nullable List<PublishPacket> publishPackets;
    private @Nullable List<SubscribePacket> subscribePackets;
    private @Nullable List<UnsubscribePacket> unsubscribePackets;
    private @Nullable List<DisconnectInformation> disconnectInformation;

    private int port = -1;
    private int tlsPort = -1;
    private int websocketsPort = -1;

    private final @NotNull TlsConfiguration tlsConfiguration;
    private final boolean websocketEnabled;

    public static @NotNull Builder builder() {
        return new Builder();
    }

    private HiveMQExtension(final @NotNull TlsConfiguration tlsConfiguration, final boolean websocketEnabled) {
        this.tlsConfiguration = tlsConfiguration;
        this.websocketEnabled = websocketEnabled;
    }

    @Override
    public void beforeAll(final @NotNull ExtensionContext context) throws IOException {
        port = generatePort();
        connectPackets = new ArrayList<>();
        connackPackets = new ArrayList<>();
        publishPackets = new ArrayList<>();
        disconnectInformation = new ArrayList<>();
        subscribePackets = new ArrayList<>();
        unsubscribePackets = new ArrayList<>();

        final String hivemqConfig = setupHivemqConfig();
        hivemqConfigFolder = Files.createTempDirectory("hivemq-config-folder");
        hivemqConfigFolder.toFile().deleteOnExit();
        final File configXml = new File(hivemqConfigFolder.toAbsolutePath().toString(), "config.xml");
        assertTrue(configXml.createNewFile());
        Files.writeString(configXml.toPath(), hivemqConfig);

        this.hivemqDataFolder = Files.createTempDirectory("hivemq-data-folder");
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
                        // Add Connect & Connack Interceptors
                        final ConnectInboundInterceptor connectInboundInterceptor =
                                (connectInboundInput, connectInboundOutput) -> connectPackets.add(connectInboundInput.getConnectPacket());
                        final ConnackOutboundInterceptor connackOutboundInterceptor =
                                (connackOutboundInput, connackOutboundOutput) -> connackPackets.add(connackOutboundInput.getConnackPacket());
                        Services.interceptorRegistry()
                                .setConnectInboundInterceptorProvider(input -> connectInboundInterceptor);
                        Services.interceptorRegistry()
                                .setConnackOutboundInterceptorProvider(input -> connackOutboundInterceptor);

                        // Add all the other interceptors
                        Services.initializerRegistry().setClientInitializer((initializerInput, clientContext) -> {

                            clientContext.addDisconnectInboundInterceptor((disconnectInboundInput, disconnectInboundOutput) -> disconnectInformation.add(
                                    new DisconnectInformation(disconnectInboundInput.getDisconnectPacket(),
                                            disconnectInboundInput.getClientInformation().getClientId())));

                            clientContext.addPublishInboundInterceptor((publishInboundInput, publishInboundOutput) -> publishPackets.add(
                                    publishInboundInput.getPublishPacket()));

                            clientContext.addSubscribeInboundInterceptor((subscribeInboundInput, subscribeInboundOutput) -> subscribePackets.add(
                                    subscribeInboundInput.getSubscribePacket()));

                            clientContext.addUnsubscribeInboundInterceptor((unsubscribeInboundInput, unsubscribeInboundOutput) -> unsubscribePackets.add(
                                    unsubscribeInboundInput.getUnsubscribePacket()));

                        });

                        Services.securityRegistry()
                                .setAuthorizerProvider(authorizerProviderInput -> PublishSubscriptionAuthorizer.INSTANCE);
                    }

                    @Override
                    public void extensionStop(
                            final @NotNull ExtensionStopInput extensionStopInput,
                            final @NotNull ExtensionStopOutput extensionStopOutput) {
                    }
                })
                .build();

        final EmbeddedHiveMQBuilder builder = EmbeddedHiveMQ.builder()
                .withConfigurationFolder(hivemqConfigFolder)
                .withDataFolder(hivemqDataFolder)
                .withEmbeddedExtension(embeddedExtension);

        try {
            hivemq = builder.build();
            InternalConfigurations.PAYLOAD_PERSISTENCE_TYPE.set(PersistenceType.FILE);
            InternalConfigurations.RETAINED_MESSAGE_PERSISTENCE_TYPE.set(PersistenceType.FILE);
            hivemq.start().get();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void afterAll(final @NotNull ExtensionContext context)
            throws IOException, ExecutionException, InterruptedException {
        Objects.requireNonNull(hivemq).stop().get();
        //This allows gc of embedded hivemq even if the JUnit Extension is still referenced (static)
        hivemq = null;
        FileUtils.deleteDirectory(Objects.requireNonNull(hivemqConfigFolder).toFile());
        FileUtils.deleteDirectory(Objects.requireNonNull(hivemqDataFolder).toFile());
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        Objects.requireNonNull(connectPackets).clear();
        Objects.requireNonNull(connackPackets).clear();
        Objects.requireNonNull(disconnectInformation).clear();
        Objects.requireNonNull(publishPackets).clear();
        Objects.requireNonNull(subscribePackets).clear();
        Objects.requireNonNull(unsubscribePackets).clear();
        setAuthorized(true);
    }

    public @NotNull List<ConnectPacket> getConnectPackets() {
        return Objects.requireNonNull(connectPackets);
    }

    public @NotNull List<ConnackPacket> getConnackPackets() {
        return Objects.requireNonNull(connackPackets);
    }

    public @NotNull List<PublishPacket> getPublishPackets() {
        return Objects.requireNonNull(publishPackets);
    }

    public @NotNull List<SubscribePacket> getSubscribePackets() {
        return Objects.requireNonNull(subscribePackets);
    }

    public @NotNull List<UnsubscribePacket> getUnsubscribePackets() {
        return Objects.requireNonNull(unsubscribePackets);
    }

    public @NotNull List<DisconnectInformation> getDisconnectInformation() {
        return Objects.requireNonNull(disconnectInformation);
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

    public void setAuthorized(final boolean isAuthorized) {
        PublishSubscriptionAuthorizer.INSTANCE.getIsAuthorized().set(isAuthorized);
    }

    private int generatePort() throws IOException {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private @NotNull String setupHivemqConfig() throws IOException {
        final String tlsConfig = setupTls();
        final String websocketsConfig = setupWebsockets();
        //@formatter:off
        return "<hivemq>\n" + "    " +
                "    <listeners>\n" + "        " +
                "        <tcp-listener>\n" +
                "            <port>" + port + "</port>\n" +
                "            <bind-address>" + BIND_ADDRESS + "</bind-address>\n" +
                "        </tcp-listener>\n" +
                tlsConfig +
                websocketsConfig +
                "    </listeners>\n" +
                "</hivemq>";
        //@formatter:on
    }

    private @NotNull String setupTls() throws IOException {
        final String tlsConfig;
        if (tlsConfiguration.isTlsEnabled()) {
            this.tlsPort = generatePort();
            final String keyStorePath = Resources.getResource("tls/server/server-keystore.jks").getPath();
            final String keyStorePassword = "serverKeystorePassword";
            final String keyStorePrivatePassword = "serverKeyPassword";
            final String trustStorePath = Resources.getResource("tls/server/server-truststore.jks").getPath();
            final String trustStorePassword = "serverTruststorePassword";

            final String tlsVersionsString;
            if (!tlsConfiguration.getTlsVersions().isEmpty()) {
                final StringBuilder tlsVersions = new StringBuilder();
                for (final TlsVersion tlsVersion : tlsConfiguration.getTlsVersions()) {
                    tlsVersions.append("                   <protocol>").append(tlsVersion).append("</protocol>\n");
                }

                //@formatter:off
                tlsVersionsString =
                        "                <protocols>\n" +
                        tlsVersions +
                        "                </protocols>\n";
                //@formatter:on
            } else {
                tlsVersionsString = "";
            }

            final String cipherSuitesString;
            if (!tlsConfiguration.getCipherSuites().isEmpty()) {
                final StringBuilder cipherSuites = new StringBuilder();
                for (final String cipherSuite : tlsConfiguration.getCipherSuites()) {
                    cipherSuites.append("                   <cipher-suite>")
                            .append(cipherSuite)
                            .append("</cipher-suite>\n");
                }
                //@formatter:off
                cipherSuitesString =
                        "                <cipher-suites>\n" +
                        cipherSuites +
                        "                </cipher-suites>\n";
                //@formatter:on
            } else {
                cipherSuitesString = "";
            }

            final String clientAuthentication;
            if (tlsConfiguration.isClientAuthentication()) {
                clientAuthentication = "REQUIRED";
            } else {
                clientAuthentication = "NONE";
            }

            //@formatter:off
            tlsConfig =
                    "<tls-tcp-listener>\n" +
                    "           <port>" + tlsPort + "</port>\n" +
                    "           <bind-address>" + BIND_ADDRESS + "</bind-address>\n" +
                    "           <tls>\n" +
                    tlsVersionsString +
                    cipherSuitesString +
                    "                <keystore\n>" +
                    "                   <path>" + keyStorePath +  "</path>\n" +
                    "                   <password>" + keyStorePassword + "</password>\n" +
                    "                   <private-key-password>" + keyStorePrivatePassword + "</private-key-password>\n" +
                    "                </keystore>\n" +
                    "                <client-authentication-mode>" + clientAuthentication + "</client-authentication-mode>\n" +
                    "                <truststore>\n" +
                    "                   <path>" + trustStorePath + "</path>\n" +
                    "                   <password>" + trustStorePassword + "</password>\n" +
                    "                </truststore>\n" +
                    "           </tls>\n" +
                    "</tls-tcp-listener>\n";
            //@formatter:on
        } else {
            tlsConfig = "";
        }
        return tlsConfig;
    }

    private @NotNull String setupWebsockets() throws IOException {
        String websocketsConfig = "";
        if (websocketEnabled) {
            this.websocketsPort = generatePort();
            //@formatter:off
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
            //@formatter:on
        }
        return websocketsConfig;
    }

    public static class Builder {
        private @NotNull TlsConfiguration tlsConfiguration = TlsConfiguration.builder().build();
        private boolean websocketEnabled = false;

        private Builder() {
        }

        public @NotNull HiveMQExtension build() {
            return new HiveMQExtension(tlsConfiguration, websocketEnabled);
        }

        public @NotNull Builder withTlsConfiguration(final @NotNull TlsConfiguration tlsConfiguration) {
            this.tlsConfiguration = tlsConfiguration;
            return this;
        }

        public @NotNull Builder withWebsocketEnabled(final boolean websocketEnabled) {
            this.websocketEnabled = websocketEnabled;
            return this;
        }
    }

    private static class PublishSubscriptionAuthorizer implements PublishAuthorizer, SubscriptionAuthorizer {

        static final @NotNull PublishSubscriptionAuthorizer INSTANCE = new PublishSubscriptionAuthorizer();
        static final @NotNull String REASON_STRING = "CLI_DENY";

        private final @NotNull AtomicBoolean isAuthorized = new AtomicBoolean(true);

        private PublishSubscriptionAuthorizer() {
        }

        @Override
        public void authorizePublish(
                final @NotNull PublishAuthorizerInput publishAuthorizerInput,
                final @NotNull PublishAuthorizerOutput publishAuthorizerOutput) {
            if (isAuthorized.get()) {
                publishAuthorizerOutput.authorizeSuccessfully();
            } else {
                publishAuthorizerOutput.failAuthorization(AckReasonCode.NOT_AUTHORIZED, REASON_STRING);
            }
        }

        @Override
        public void authorizeSubscribe(
                final @NotNull SubscriptionAuthorizerInput subscriptionAuthorizerInput,
                final @NotNull SubscriptionAuthorizerOutput subscriptionAuthorizerOutput) {
            if (isAuthorized.get()) {
                subscriptionAuthorizerOutput.authorizeSuccessfully();
            } else {
                subscriptionAuthorizerOutput.failAuthorization(SubackReasonCode.NOT_AUTHORIZED, REASON_STRING);
            }
        }

        public @NotNull AtomicBoolean getIsAuthorized() {
            return isAuthorized;
        }
    }
}

