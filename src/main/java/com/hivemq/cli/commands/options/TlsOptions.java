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

package com.hivemq.cli.commands.options;

import com.google.common.base.Throwables;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.utils.PasswordUtils;
import com.hivemq.cli.utils.TlsUtil;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;

public class TlsOptions {

    private static final @NotNull String DEFAULT_TLS_VERSION = "TLSv1.2";

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--secure"},
                        defaultValue = "false",
                        description = "Use default tls configuration if no other tls options are specified (default: false)")
    private boolean useTls;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--cafile", "--ca-cert", "--server-cert"},
                        paramLabel = "FILE",
                        description = "Path to a file containing trusted CA certificates to enable encrypted certificate based communication")
    private @Nullable Path serverCertificateChain;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--capath", "--ca-cert-dir", "--server-cert-dir"}, paramLabel = "DIR", description = {
            "Path to a directory containing certificate files to import to enable encrypted certificate based communication"})
    private @Nullable Path serverCertificateChainFromDir;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--cert", "--client-cert"},
                        description = "The client certificate to use for client side authentication")
    private @Nullable Path clientCertificateChain;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--key", "--client-private-key"},
                        description = "The path to the client private key for client side authentication")
    private @Nullable Path clientPrivateKey;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--keypw", "--client-private-key-password"},
                        description = "The password for the keystore")
    private @Nullable String clientPrivateKeyPassword;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--ciphers"},
                        split = ":",
                        description = "The client supported cipher suites list in IANA format separated with ':'")
    private @Nullable Collection<String> cipherSuites;

    @CommandLine.Option(names = {"--tls-version"},
                        description = "The TLS protocol version to use (default: {'TLSv.1.2'})")
    private @Nullable Collection<String> supportedTLSVersions;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--ks", "--keystore"},
                        description = "The path to the client keystore for client side authentication")
    private @Nullable Path clientKeystore;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--kspw", "--keystore-password"}, description = "The password for the keystore")
    private @Nullable String clientKeystorePassword;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--kspkpw", "--keystore-private-key-password"},
                        description = "The password for the private key inside the keystore")
    private @Nullable String clientKeystorePrivateKeyPassword;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--ts", "--truststore"},
                        description = "The path to the client truststore to enable encrypted certificate based communication")
    private @Nullable Path clientTruststore;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--tspw", "--truststore-password"},
                        description = "The path to the client truststore for client side authentication")
    private @Nullable String clientTruststorePassword;

    public @Nullable MqttClientSslConfig buildSslConfig() throws Exception {
        if (!useTls()) {
            return null;
        }

        final KeyManagerFactory keyManagerFactory = buildKeyManagerFactory();
        final TrustManagerFactory trustManagerFactory = buildTrustManagerFactory();

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

    private @Nullable KeyManagerFactory buildKeyManagerFactory() throws Exception {
        final KeyManagerFactory keyManagerFactory;

        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTTCLI).defaultCLIProperties();
        final Path defaultKeystore = defaultCLIProperties.getKeystore();
        final Path defaultClientCertificateChain = defaultCLIProperties.getClientCertificateChain();
        final Path defaultClientPrivateKey = defaultCLIProperties.getClientPrivateKey();

        //keystore
        if (clientKeystore != null) {
            keyManagerFactory = createKeyManagerFactoryFromKeystore(clientKeystore,
                    clientKeystorePassword,
                    clientKeystorePrivateKeyPassword);
            //certificate and private key file
        } else if (clientCertificateChain != null && clientPrivateKey != null) {
            final TlsUtil tlsUtil = new TlsUtil();
            final Collection<X509Certificate> x509Certificates =
                    tlsUtil.getCertificateChainFromFile(clientCertificateChain);
            final PrivateKey privateKey = tlsUtil.getPrivateKeyFromFile(clientPrivateKey, clientPrivateKeyPassword);
            final String password = "fillerPassword";
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setKeyEntry("mykey",
                    privateKey,
                    password.toCharArray(),
                    x509Certificates.toArray(new X509Certificate[0]));

            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, password.toCharArray());
            //default keystore
        } else if (defaultKeystore != null) {
            final String defaultKeystorePassword = defaultCLIProperties.getKeystorePassword();
            final String defaultKeystorePrivateKeyPassword = defaultCLIProperties.getKeystorePrivateKeyPassword();
            try {
                keyManagerFactory = createKeyManagerFactoryFromKeystore(defaultKeystore,
                        defaultKeystorePassword,
                        defaultKeystorePrivateKeyPassword);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default keystore could not be loaded ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            //default certificate and private key file
        } else if (defaultClientCertificateChain != null && defaultClientPrivateKey != null) {
            final TlsUtil tlsUtil = new TlsUtil();
            final Collection<X509Certificate> x509Certificates;
            try {
                x509Certificates = tlsUtil.getCertificateChainFromFile(defaultClientCertificateChain);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default client certificate chain could not be loaded ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            final PrivateKey privateKey;
            final String defaultClientPrivateKeyPassword = defaultCLIProperties.getClientPrivateKeyPassword();
            try {
                privateKey = tlsUtil.getPrivateKeyFromFile(defaultClientPrivateKey, defaultClientPrivateKeyPassword);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default client private key could not be loaded ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            final String password = "filler";
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setKeyEntry("mykey",
                    privateKey,
                    password.toCharArray(),
                    x509Certificates.toArray(new X509Certificate[0]));

            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, password.toCharArray());
            //nothing found
        } else {
            keyManagerFactory = null;
        }
        return keyManagerFactory;
    }

    private @NotNull KeyManagerFactory createKeyManagerFactoryFromKeystore(
            final @NotNull Path clientKeystore,
            final @Nullable String clientKeystorePassword,
            final @Nullable String clientKeystorePrivateKeyPassword)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException,
            UnrecoverableKeyException {
        final KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (TlsUtil.isPKCS12Keystore(clientKeystore)) {
            final KeyStore keyStore = KeyStore.getInstance("PKCS12");
            final char[] keystorePassword;
            if (clientKeystorePassword != null) {
                keystorePassword = clientKeystorePassword.toCharArray();
            } else {
                keystorePassword = PasswordUtils.readPassword("Enter keystore password:");
            }
            try (final InputStream inputStream = Files.newInputStream(clientKeystore, StandardOpenOption.READ)) {
                keyStore.load(inputStream, keystorePassword);
            }
            keyManagerFactory.init(keyStore, keystorePassword);
        } else if (TlsUtil.isJKSKeystore(clientKeystore)) {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final char[] keystorePassword;
            if (clientKeystorePassword != null) {
                keystorePassword = clientKeystorePassword.toCharArray();
            } else {
                keystorePassword = PasswordUtils.readPassword("Enter keystore password:");
            }
            try (final InputStream inputStream = Files.newInputStream(clientKeystore, StandardOpenOption.READ)) {
                keyStore.load(inputStream, keystorePassword);
            }

            boolean isDifferentPrivateKeyPassword = false;
            try {
                keyManagerFactory.init(keyStore, keystorePassword);
            } catch (final UnrecoverableKeyException differentPrivateKeyPassword) {
                isDifferentPrivateKeyPassword = true;
            }
            if (isDifferentPrivateKeyPassword) {
                final char[] keystorePrivateKeyPassword;
                if (clientKeystorePrivateKeyPassword != null) {
                    keystorePrivateKeyPassword = clientKeystorePrivateKeyPassword.toCharArray();
                } else {
                    keystorePrivateKeyPassword = PasswordUtils.readPassword("Enter keystore private key password:");
                }
                keyManagerFactory.init(keyStore, keystorePrivateKeyPassword);
            }
        } else {
            throw new KeyStoreException("Unknown keystore type. Please use a PKCS#12 (.p12/.pfx) or JKS (.jks) keystore.");
        }
        return keyManagerFactory;
    }


    private @Nullable TrustManagerFactory buildTrustManagerFactory() throws Exception {
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTTCLI).defaultCLIProperties();
        TrustManagerFactory trustManagerFactory = null;
        KeyStore keyStore = null;
        if (clientTruststore != null) {
            if (TlsUtil.isPKCS12Keystore(clientTruststore)) {
                keyStore = KeyStore.getInstance("PKCS12");
                final char[] keystorePassword;
                if (clientTruststorePassword != null) {
                    keystorePassword = clientTruststorePassword.toCharArray();
                } else {
                    keystorePassword = PasswordUtils.readPassword("Enter truststore password:");
                }
                try (final InputStream inputStream = Files.newInputStream(clientTruststore, StandardOpenOption.READ)) {
                    keyStore.load(inputStream, keystorePassword);
                }
            } else if (TlsUtil.isJKSKeystore(clientTruststore)) {
                keyStore = KeyStore.getInstance("JKS");
                final char[] truststorePassword;
                if (clientTruststorePassword != null) {
                    truststorePassword = clientTruststorePassword.toCharArray();
                } else {
                    truststorePassword = PasswordUtils.readPassword("Enter truststore password:");
                }
                //TODO could also need a separate private key check
                try (final InputStream inputStream = Files.newInputStream(clientTruststore, StandardOpenOption.READ)) {
                    keyStore.load(inputStream, truststorePassword);
                }
            } else {
                throw new KeyStoreException(
                        "Unknown truststore type. Please use a PKCS#12 (.p12/.pfx) or JKS (.jks) truststore.");
            }
        }

        final Path defaultTruststore = defaultCLIProperties.getTruststore();
        final String defaultTruststorePassword = defaultCLIProperties.getTruststorePassword();
        if (defaultTruststore != null) {
            if (TlsUtil.isPKCS12Keystore(defaultTruststore)) {
                final KeyStore temporaryKeystore = KeyStore.getInstance("PKCS12");
                final char[] keystorePassword;
                if (defaultTruststorePassword != null) {
                    keystorePassword = defaultTruststorePassword.toCharArray();
                } else {
                    keystorePassword = PasswordUtils.readPassword("Enter truststore password:");
                }
                try (final InputStream inputStream = Files.newInputStream(defaultTruststore, StandardOpenOption.READ)) {
                    temporaryKeystore.load(inputStream, keystorePassword);
                }
                if (keyStore != null) {
                    for (final Enumeration<String> aliases = temporaryKeystore.aliases(); aliases.hasMoreElements(); ) {
                        final String alias = aliases.nextElement();
                        keyStore.setCertificateEntry("Truststore_Default_Certificate_" + alias,
                                temporaryKeystore.getCertificate(alias));
                    }
                } else {
                    keyStore = temporaryKeystore;
                }
            } else if (TlsUtil.isJKSKeystore(defaultTruststore)) {
                final KeyStore temporaryKeystore = KeyStore.getInstance("JKS");
                final char[] truststorePassword;
                if (defaultTruststorePassword != null) {
                    truststorePassword = defaultTruststorePassword.toCharArray();
                } else {
                    truststorePassword = PasswordUtils.readPassword("Enter truststore password:");
                }
                //TODO could also need a separate private key check
                try (final InputStream inputStream = Files.newInputStream(defaultTruststore, StandardOpenOption.READ)) {
                    temporaryKeystore.load(inputStream, truststorePassword);
                }
                if (keyStore != null) {
                    for (final Enumeration<String> aliases = temporaryKeystore.aliases(); aliases.hasMoreElements(); ) {
                        final String alias = aliases.nextElement();
                        keyStore.setCertificateEntry("Truststore_Default_Certificate_" + alias,
                                temporaryKeystore.getCertificate(alias));
                    }
                } else {
                    keyStore = temporaryKeystore;
                }
            } else {
                throw new KeyStoreException(
                        "Unknown default truststore type. Please use a PKCS#12 (.p12/.pfx) or JKS (.jks) truststore.");
            }
        }

        final TlsUtil tlsUtil = new TlsUtil();
        { //Load server certificates from file or directory
            final Collection<X509Certificate> certificates = new ArrayList<>();
            if (serverCertificateChain != null) {
                certificates.addAll(tlsUtil.getCertificateChainFromFile(serverCertificateChain));
            }
            if (serverCertificateChainFromDir != null) {
                certificates.addAll(tlsUtil.getCertificateChainFromDirectory(serverCertificateChainFromDir));
            }
            if (!certificates.isEmpty()) {
                if (keyStore == null) {
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                }

                int i = 1;
                for (final X509Certificate cert : certificates) {
                    final String alias = "Additional_Certificate_" + i;
                    keyStore.setCertificateEntry(alias, cert);
                    i++;
                }
            }
        }

        { //Load default server certificate
            final Path defaultServerCertificate = defaultCLIProperties.getServerCertificateChain();
            if (defaultServerCertificate != null) {
                final Collection<X509Certificate> defaultCertificates = new ArrayList<>();
                try {
                    final Collection<X509Certificate> defaultCertificateChain =
                            tlsUtil.getCertificateChainFromFile(defaultServerCertificate);
                    defaultCertificates.addAll(defaultCertificateChain);
                } catch (final Exception e) {
                    Logger.error(e,
                            "Default server certificate could not be loaded. Ignoring. ({})",
                            Throwables.getRootCause(e).getMessage());
                }
                if (!defaultCertificates.isEmpty()) {
                    if (keyStore == null) {
                        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keyStore.load(null, null);
                    }

                    int i = 1;
                    for (final X509Certificate cert : defaultCertificates) {
                        final String alias = "File_Default_Certificate_" + i;
                        keyStore.setCertificateEntry(alias, cert);
                        i++;
                    }
                }
            }
        }

        if (keyStore != null) {
            trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
        }
        return trustManagerFactory;
    }

    private boolean useTls() {
        return clientKeystore != null ||
                clientTruststore != null ||
                serverCertificateChain != null ||
                serverCertificateChainFromDir != null ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientPrivateKey != null ||
                clientCertificateChain != null ||
                useTls;
    }

    @Override
    public @NotNull String toString() {
        return "TlsOptions{" +
                "useSsl=" +
                useTls +
                ", serverCertificates=" +
                serverCertificateChain +
                ", serverCertificatesFromDir=" +
                serverCertificateChainFromDir +
                ", cipherSuites=" +
                cipherSuites +
                ", supportedTLSVersions=" +
                supportedTLSVersions +
                ", clientCertificates=" +
                clientCertificateChain +
                ", clientPrivateKey=" +
                clientPrivateKey +
                '}';
    }
}
