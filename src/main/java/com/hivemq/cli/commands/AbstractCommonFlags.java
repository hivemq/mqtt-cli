/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.commands;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.DirectoryToCertificateCollectionConverter;
import com.hivemq.cli.converters.EnvVarToByteBufferConverter;
import com.hivemq.cli.converters.FileToCertificateConverter;
import com.hivemq.cli.converters.FileToPrivateKeyConverter;
import com.hivemq.cli.converters.PasswordFileToByteBufferConverter;
import com.hivemq.cli.converters.UnsignedShortConverter;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractCommonFlags extends AbstractConnectRestrictionFlags implements Connect {

    private static final String DEFAULT_TLS_VERSION = "TLSv1.2";

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for authentication", order = 2)
    @Nullable
    private String user;

    @CommandLine.Option(names = {"-pw", "--password"}, arity = "0..1", interactive = true, converter = ByteBufferConverter.class, description = "The password for authentication", order = 2)
    @Nullable
    private ByteBuffer password;

    @CommandLine.Option(names = {"-pw:env"}, arity = "0..1", converter = EnvVarToByteBufferConverter.class, fallbackValue = "MQTT_CLI_PW", description = "The password for authentication read in from an environment variable", order = 2)
    private void setPasswordFromEnv(final @NotNull ByteBuffer passwordEnvironmentVariable) { password = passwordEnvironmentVariable; }

    @CommandLine.Option(names = {"-pw:file"}, converter = PasswordFileToByteBufferConverter.class, description = "The password for authentication read in from a file", order = 2)
    private void setPasswordFromFile(final @NotNull ByteBuffer passwordFromFile) { password = passwordFromFile; }

    @CommandLine.Option(names = {"-k", "--keepAlive"}, converter = UnsignedShortConverter.class, description = "A keep alive of the client (in seconds) (default: 60)", order = 2)
    private @Nullable Integer keepAlive;

    @CommandLine.Option(names = {"-c", "--cleanStart"}, negatable = true, description = "Define a clean start for the connection (default: true)", order = 2)
    @Nullable
    private Boolean cleanStart;

    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use default ssl configuration if no other ssl options are specified (default: false)", order = 2)
    private boolean useSsl;

    @CommandLine.Option(names = {"--cafile"}, paramLabel = "FILE", converter = FileToCertificateConverter.class, description = "Path to a file containing trusted CA certificates to enable encrypted certificate based communication", order = 2)
    @Nullable
    private Collection<X509Certificate> certificates;

    @CommandLine.Option(names = {"--capath"}, paramLabel = "DIR", converter = DirectoryToCertificateCollectionConverter.class, description = {"Path to a directory containing certificate files to import to enable encrypted certificate based communication"}, order = 2)
    @Nullable
    private Collection<X509Certificate> certificatesFromDir;

    @CommandLine.Option(names = {"--ciphers"}, split = ":", description = "The client supported cipher suites list in IANA format separated with ':'", order = 2)
    @Nullable
    private Collection<String> cipherSuites;

    @CommandLine.Option(names = {"--tls-version"}, description = "The TLS protocol version to use (default: {'TLSv.1.2'})", order = 2)
    @Nullable
    private Collection<String> supportedTLSVersions;

    @CommandLine.Option(names = {"--cert"}, converter = FileToCertificateConverter.class, description = "The client certificate to use for client side authentication", order = 2)
    @Nullable
    private X509Certificate clientCertificate;

    @CommandLine.Option(names = {"--key"}, converter = FileToPrivateKeyConverter.class, description = "The path to the client private key for client side authentication", order = 2)
    @Nullable
    private PrivateKey clientPrivateKey;

    @CommandLine.Option(names = {"-ws"}, description = "Use WebSocket transport protocol (default: false)", order = 2)
    private boolean useWebSocket;

    @CommandLine.Option(names = {"-ws:path"}, description = "The path of the WebSocket", order = 2)
    @Nullable private String webSocketPath;

    @Override
    public void setDefaultOptions() {
        super.setDefaultOptions();
        final DefaultCLIProperties defaultCLIProperties = MqttCLIMain.MQTTCLI.defaultCLIProperties();

        if (user == null) {
            user = defaultCLIProperties.getUsername();
        }

        if (password == null) {
            try {
                password = defaultCLIProperties.getPassword();
            } catch (Exception e) {
                Logger.error(e,"Default password could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }

        if (clientCertificate == null) {
            try {
                clientCertificate = defaultCLIProperties.getClientCertificate();
            } catch (Exception e) {
                Logger.error(e,"Default client certificate could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }

        if (clientPrivateKey == null) {
            try {
                clientPrivateKey = defaultCLIProperties.getClientPrivateKey();
            } catch (Exception e) {
                Logger.error(e,"Default client private key could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }

        if (useWebSocket && webSocketPath == null) {
            webSocketPath = defaultCLIProperties.getWebsocketPath();
        }

        try {
            final X509Certificate defaultServerCertificate = defaultCLIProperties.getServerCertificate();
            if (defaultServerCertificate != null) {
                if(certificates == null){
                    certificates = new ArrayList<>();
                }
                certificates.add(defaultServerCertificate);
            }
        } catch (Exception e) {
            Logger.error(e,"Default server certificate could not be loaded ({})", Throwables.getRootCause(e).getMessage());
        }

    }


    public @Nullable MqttClientSslConfig buildSslConfig() {

        if (useBuiltSslConfig()) {
            try {
                return doBuildSslConfig();
            }
            catch (Exception e) {
                Logger.error(e, Throwables.getRootCause(e).getMessage());
            }
        }

        return null;
    }

    private boolean useBuiltSslConfig() {
        return certificates != null ||
                certificatesFromDir != null ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientPrivateKey != null ||
                clientCertificate != null ||
                useSsl;
    }

    private @NotNull MqttClientSslConfig doBuildSslConfig() throws Exception {

        if (certificatesFromDir != null) {
            if (certificates == null) {
                certificates = certificatesFromDir;
            }
            else {
                certificates.addAll(certificatesFromDir);
            }
        }


        // build trustManagerFactory for server side authentication and to enable tls
        TrustManagerFactory trustManagerFactory = null;
        if (certificates != null && !certificates.isEmpty()) {
            trustManagerFactory = buildTrustManagerFactory(certificates);
        }


        // build keyManagerFactory if clientSideAuthentication is used
        KeyManagerFactory keyManagerFactory = null;
        if (clientCertificate != null && clientPrivateKey != null) {
            keyManagerFactory = buildKeyManagerFactory(clientCertificate, clientPrivateKey);
        }

        // default to tlsv.2
        if (supportedTLSVersions == null) {
            supportedTLSVersions = new ArrayList<>();
            supportedTLSVersions.add(DEFAULT_TLS_VERSION);
        }

        return MqttClientSslConfig.builder()
                .trustManagerFactory(trustManagerFactory)
                .keyManagerFactory(keyManagerFactory)
                .cipherSuites(cipherSuites)
                .protocols(supportedTLSVersions)
                .build();
    }


    private TrustManagerFactory buildTrustManagerFactory(final @NotNull Collection<X509Certificate> certCollection) throws Exception {

        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);

        // add all certificates of the collection to the KeyStore
        int i = 1;
        for (final X509Certificate cert : certCollection) {
            final String alias = Integer.toString(i);
            ks.setCertificateEntry(alias, cert);
            i++;
        }

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        trustManagerFactory.init(ks);

        return trustManagerFactory;
    }

    private KeyManagerFactory buildKeyManagerFactory(final @NotNull X509Certificate cert, final @NotNull PrivateKey key) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {

        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(null, null);

        final Certificate[] certChain = new Certificate[1];
        certChain[0] = cert;
        ks.setKeyEntry("mykey", key, null, certChain);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(ks, null);

        return keyManagerFactory;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    @Override
    public String toString() {

        return "Connect{" +
                "key=" + getKey() +
                ", " + commonOptions() +
                '}';
    }


    public String commonOptions() {
        return super.toString() +
                (user != null ? (", user=" + user) : "") +
                (keepAlive != null ? (", keepAlive=" + keepAlive) : "") +
                (cleanStart != null ? (", cleanStart=" + cleanStart) : "") +
                ", useDefaultSsl=" + useSsl +
                (getSslConfig() != null ? (", sslConfig=" + getSslConfig()) : "") +
                ", useWebSocket=" + useWebSocket +
                (webSocketPath != null ? (", webSocketPath=" + webSocketPath) : "") +
                getWillOptions();
    }

    @Nullable
    public String getUser() {
        return user;
    }

    public void setUser(final @Nullable String user) {
        this.user = user;
    }

    @Nullable
    public ByteBuffer getPassword() {
        return password;
    }

    @Nullable
    public Integer getKeepAlive() {
        return keepAlive;
    }

    @Nullable
    public Boolean getCleanStart() {
        return cleanStart;
    }

    @Nullable
    public MqttWebSocketConfig getWebSocketConfig() {
        if (useWebSocket) {
            return MqttWebSocketConfig.builder()
                    .serverPath(webSocketPath)
                    .build();
        }
        else {
            return null;
        }
    }

}
