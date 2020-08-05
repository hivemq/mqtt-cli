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

import com.hivemq.cli.converters.DirectoryToCertificateCollectionConverter;
import com.hivemq.cli.converters.FileToCertificateConverter;
import com.hivemq.cli.converters.FileToPrivateKeyConverter;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
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

public class SslOptions {

    private static final String DEFAULT_TLS_VERSION = "TLSv1.2";

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

    private boolean useBuiltSslConfig() {
        return certificates != null ||
                certificatesFromDir != null ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientPrivateKey != null ||
                clientCertificate != null ||
                useSsl;
    }

    public @Nullable MqttClientSslConfig buildSslConfig() throws Exception {

        if (!useBuiltSslConfig()) {
            return null;
        }

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

    @Override
    public String toString() {
        return "SslOptions{" +
                "useSsl=" + useSsl +
                ", certificates=" + certificates +
                ", certificatesFromDir=" + certificatesFromDir +
                ", cipherSuites=" + cipherSuites +
                ", supportedTLSVersions=" + supportedTLSVersions +
                ", clientCertificate=" + clientCertificate +
                ", clientPrivateKey=" + clientPrivateKey +
                '}';
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public @Nullable Collection<X509Certificate> getCertificates() { return certificates; }

    public @Nullable Collection<X509Certificate> getCertificatesFromDir() { return certificatesFromDir; }

    public @Nullable Collection<String> getCipherSuites() { return cipherSuites; }

    public @Nullable Collection<String> getSupportedTLSVersions() { return supportedTLSVersions; }

    public @Nullable X509Certificate getClientCertificate() { return clientCertificate; }

    public @Nullable PrivateKey getClientPrivateKey() { return clientPrivateKey; }
}
