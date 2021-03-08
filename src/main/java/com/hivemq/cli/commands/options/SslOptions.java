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
package com.hivemq.cli.commands.options;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.converters.DirectoryToCertificatesConverter;
import com.hivemq.cli.converters.FileToCertificatesConverter;
import com.hivemq.cli.converters.FileToPrivateKeyConverter;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

public class SslOptions {

    private static final String DEFAULT_TLS_VERSION = "TLSv1.2";

    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use default ssl configuration if no other ssl options are specified (default: false)", order = 2)
    private boolean useSsl;

    @CommandLine.Option(names = {"--cafile"}, paramLabel = "FILE", converter = FileToCertificatesConverter.class, description = "Path to a file containing trusted CA certificates to enable encrypted certificate based communication", order = 2)
    @Nullable
    private Collection<X509Certificate> serverCertificateChain;

    @CommandLine.Option(names = {"--capath"}, paramLabel = "DIR", converter = DirectoryToCertificatesConverter.class, description = {"Path to a directory containing certificate files to import to enable encrypted certificate based communication"}, order = 2)
    @Nullable
    private Collection<X509Certificate> serverCertificateChainFromDir;

    @CommandLine.Option(names = {"--ciphers"}, split = ":", description = "The client supported cipher suites list in IANA format separated with ':'", order = 2)
    @Nullable
    private Collection<String> cipherSuites;

    @CommandLine.Option(names = {"--tls-version"}, description = "The TLS protocol version to use (default: {'TLSv.1.2'})", order = 2)
    @Nullable
    private Collection<String> supportedTLSVersions;

    @CommandLine.Option(names = {"--cert"}, converter = FileToCertificatesConverter.class, description = "The client certificate to use for client side authentication", order = 2)
    @Nullable
    private Collection<X509Certificate> clientCertificateChain;

    @CommandLine.Option(names = {"--key"}, converter = FileToPrivateKeyConverter.class, description = "The path to the client private key for client side authentication", order = 2)
    @Nullable
    private PrivateKey clientPrivateKey;

    private boolean useBuiltSslConfig() {
        return serverCertificateChain != null ||
                serverCertificateChainFromDir != null ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientPrivateKey != null ||
                clientCertificateChain != null ||
                useSsl;
    }

    public @Nullable MqttClientSslConfig buildSslConfig() throws Exception {

        setDefaultOptions();

        if (!useBuiltSslConfig()) {
            return null;
        }

        if (serverCertificateChainFromDir != null) {
            if (serverCertificateChain == null) {
                serverCertificateChain = serverCertificateChainFromDir;
            }
            else {
                serverCertificateChain.addAll(serverCertificateChainFromDir);
            }
        }


        // build trustManagerFactory for server side authentication and to enable tls
        TrustManagerFactory trustManagerFactory = null;
        if (serverCertificateChain != null && !serverCertificateChain.isEmpty()) {
            trustManagerFactory = buildTrustManagerFactory(serverCertificateChain);
        }


        // build keyManagerFactory if clientSideAuthentication is used
        KeyManagerFactory keyManagerFactory = null;
        if (clientCertificateChain != null && clientPrivateKey != null) {
            keyManagerFactory = buildKeyManagerFactory(clientCertificateChain.toArray(new X509Certificate[0]), clientPrivateKey);
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

    private void setDefaultOptions() {
        final DefaultCLIProperties defaultCLIProperties = MqttCLIMain.MQTTCLI.defaultCLIProperties();

        if (clientCertificateChain == null) {
            try {
                clientCertificateChain = defaultCLIProperties.getClientCertificateChain();
            } catch (final Exception e) {
                Logger.error(e,"Default client certificate chain could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }

        if (clientPrivateKey == null) {
            try {
                clientPrivateKey = defaultCLIProperties.getClientPrivateKey();
            } catch (final Exception e) {
                Logger.error(e,"Default client private key could not be loaded ({})", Throwables.getRootCause(e).getMessage());
            }
        }

        try {
            final Collection<X509Certificate> defaultServerCertificate = defaultCLIProperties.getServerCertificateChain();
            if (defaultServerCertificate != null) {
                if(serverCertificateChain == null){
                    serverCertificateChain = new ArrayList<>();
                }
                serverCertificateChain.addAll(defaultServerCertificate);
            }
        } catch (final Exception e) {
            Logger.error(e,"Default server certificate could not be loaded ({})", Throwables.getRootCause(e).getMessage());
        }
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

    private KeyManagerFactory buildKeyManagerFactory(final @NotNull X509Certificate[] certs, final @NotNull PrivateKey key) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {

        final String password = "PA$$WORD";
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(null, null);

        ks.setKeyEntry("mykey", key, password.toCharArray(), certs);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(ks, password.toCharArray());

        return keyManagerFactory;
    }

    @Override
    public String toString() {
        return "SslOptions{" +
                "useSsl=" + useSsl +
                ", serverCertificates=" + serverCertificateChain +
                ", serverCertificatesFromDir=" + serverCertificateChainFromDir +
                ", cipherSuites=" + cipherSuites +
                ", supportedTLSVersions=" + supportedTLSVersions +
                ", clientCertificates=" + clientCertificateChain +
                ", clientPrivateKey=" + clientPrivateKey +
                '}';
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public @Nullable Collection<X509Certificate> getServerCertificateChain() { return serverCertificateChain; }

    public @Nullable Collection<X509Certificate> getServerCertificateChainFromDir() { return serverCertificateChainFromDir; }

    public @Nullable Collection<String> getCipherSuites() { return cipherSuites; }

    public @Nullable Collection<String> getSupportedTLSVersions() { return supportedTLSVersions; }

    public @Nullable Collection<X509Certificate> getClientCertificateChain() { return clientCertificateChain; }

    public @Nullable PrivateKey getClientPrivateKey() { return clientPrivateKey; }
}
