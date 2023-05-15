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
import com.hivemq.cli.utils.TlsUtil;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
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
    private @Nullable Path serverCertificatePath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--capath", "--ca-cert-dir", "--server-cert-dir"}, paramLabel = "DIR", description = {
            "Path to a directory containing certificate files to import to enable encrypted certificate based communication"})
    private @Nullable Path serverCertificateDirPath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--cert", "--client-cert"},
                        description = "The client certificate to use for client side authentication")
    private @Nullable Path clientCertificatePath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--key", "--client-private-key"},
                        description = "The path to the client private key for client side authentication")
    private @Nullable Path clientPrivateKeyPath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--keypw", "--client-private-key-password"},
                        description = "The password for the client private key")
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
    private @Nullable Path clientKeystorePath;

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
    private @Nullable Path clientTruststorePath;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--tspw", "--truststore-password"}, description = "The password for the truststore")
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

    /**
     * The keystore loading uses a natural hierarchy to determine precedence over the security provider possibilities.
     * <ol>
     *     <li>A keystore provided as input parameter</li>
     *     <li>A private key and related certificate provided as input parameter</li>
     *     <li>A keystore provided as default option inside the cli configuration file</li>
     *     <li>A private key and related certificate provided as default option inside the cli configuration file</li>
     * </ol>
     *
     * @return a key manager factory or null if no key provider was configured.
     * @throws Exception if a keystore or private key could not be accessed.
     */
    private @Nullable KeyManagerFactory buildKeyManagerFactory() throws Exception {
        final KeyManagerFactory keyManagerFactory;

        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();
        final Path defaultKeystore = defaultCLIProperties.getKeystore();
        final Path defaultClientCertificateChain = defaultCLIProperties.getClientCertificateChain();
        final Path defaultClientPrivateKey = defaultCLIProperties.getClientPrivateKey();

        final boolean keystoreExists = clientKeystorePath != null;
        final boolean certificateAndPrivateKeyExists = clientCertificatePath != null && clientPrivateKeyPath != null;
        final boolean defaultKeystoreExists = defaultKeystore != null;
        final boolean defaultCertificateAndPrivateKeyExists =
                defaultClientCertificateChain != null && defaultClientPrivateKey != null;

        validateKeyManagerProviders(defaultCLIProperties);

        if (keystoreExists) {
            keyManagerFactory = TlsUtil.createKeyManagerFactoryFromKeystore(clientKeystorePath,
                    clientKeystorePassword,
                    clientKeystorePrivateKeyPassword);
        } else if (certificateAndPrivateKeyExists) {
            final Collection<X509Certificate> x509Certificates =
                    TlsUtil.getCertificateChainFromFile(clientCertificatePath);
            final PrivateKey privateKey = TlsUtil.getPrivateKeyFromFile(clientPrivateKeyPath, clientPrivateKeyPassword);
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            final String createdKeystoreName = "privateKeyAlias";
            final String createdKeystorePassword = "keystorePassword";
            ks.setKeyEntry(createdKeystoreName,
                    privateKey,
                    createdKeystorePassword.toCharArray(),
                    x509Certificates.toArray(new X509Certificate[0]));

            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, createdKeystorePassword.toCharArray());
        } else if (defaultKeystoreExists) {
            final String defaultKeystorePassword = defaultCLIProperties.getKeystorePassword();
            final String defaultKeystorePrivateKeyPassword = defaultCLIProperties.getKeystorePrivateKeyPassword();
            try {
                keyManagerFactory = TlsUtil.createKeyManagerFactoryFromKeystore(defaultKeystore,
                        defaultKeystorePassword,
                        defaultKeystorePrivateKeyPassword);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default keystore ('auth.keystore') could not be loaded from '" +
                                defaultKeystore.toAbsolutePath() +
                                "'. ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
        } else if (defaultCertificateAndPrivateKeyExists) {
            final Collection<X509Certificate> x509Certificates;
            try {
                x509Certificates = TlsUtil.getCertificateChainFromFile(defaultClientCertificateChain);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default client certificate chain ('auth.client.cert') could not be loaded '" +
                                defaultClientCertificateChain.toAbsolutePath() +
                                "'. ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            final PrivateKey privateKey;
            final String defaultClientPrivateKeyPassword = defaultCLIProperties.getClientPrivateKeyPassword();
            try {
                privateKey = TlsUtil.getPrivateKeyFromFile(defaultClientPrivateKey, defaultClientPrivateKeyPassword);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default client private key ('auth.client.key') could not be loaded '" +
                                defaultClientPrivateKey.toAbsolutePath() +
                                "'. ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            final String createdKeystoreName = "privateKeyAlias";
            final String createdKeystorePassword = "keystorePassword";
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setKeyEntry(createdKeystoreName,
                    privateKey,
                    createdKeystorePassword.toCharArray(),
                    x509Certificates.toArray(new X509Certificate[0]));

            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(ks, createdKeystorePassword.toCharArray());
        } else {
            keyManagerFactory = null;
        }
        return keyManagerFactory;
    }

    private void validateKeyManagerProviders(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        final Path defaultKeystore = defaultCLIProperties.getKeystore();
        final Path defaultClientCertificateChain = defaultCLIProperties.getClientCertificateChain();
        final Path defaultClientPrivateKey = defaultCLIProperties.getClientPrivateKey();

        final boolean keystoreExists = clientKeystorePath != null;
        final boolean certificateAndPrivateKeyExists = clientCertificatePath != null && clientPrivateKeyPath != null;
        final boolean onlyCertificateExists = clientCertificatePath != null && clientPrivateKeyPath == null;
        final boolean onlyPrivateKeyExists = clientCertificatePath == null && clientPrivateKeyPath != null;

        final boolean defaultKeystoreExists = defaultKeystore != null;
        final boolean onlyDefaultCertificateExists =
                defaultClientCertificateChain != null && defaultClientPrivateKey == null;
        final boolean onlyDefaultPrivateKeyExists =
                defaultClientCertificateChain == null && defaultClientPrivateKey != null;
        final boolean defaultCertificateAndPrivateKeyExists =
                defaultClientCertificateChain != null && defaultClientPrivateKey != null;

        if (keystoreExists) {
            if (certificateAndPrivateKeyExists) {
                throw new RuntimeException("The keystore parameter ('--ks', '--keystore') is used together with " +
                        "the private key ('--key', '--client-private-key') and related client certificate " +
                        "('--cert', '--client-cert') parameters. Please only use one of the two methods.");
            }
        } else if (onlyCertificateExists) {
            throw new RuntimeException("Only the client certificate parameter exists. Please add the private key " +
                    "('--key', '--client-private-key') parameter as well.");
        } else if (onlyPrivateKeyExists) {
            throw new RuntimeException("Only the private key parameter exists. Please add the client certificate " +
                    "('--cert', '--client-cert') parameter as well.");
        } else if (certificateAndPrivateKeyExists) {
            //expected behavior
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (defaultKeystoreExists) {
            if (defaultCertificateAndPrivateKeyExists) {
                throw new RuntimeException("The keystore default option ('auth.keystore') is used together with " +
                        "the private key and related certificate default options ('auth.client.key', 'auth.client.cert'). " +
                        "Please only use one of the two methods inside the configuration file '" +
                        Objects.requireNonNull(defaultCLIProperties.getFile()).getAbsolutePath() +
                        "'.");
            }
        } else if (onlyDefaultCertificateExists) {
            throw new RuntimeException("Only the client certificate default option ('auth.client.cert') exists. " +
                    "Please add the private key default option ('auth.client.key') inside the configuration file '" +
                    Objects.requireNonNull(defaultCLIProperties.getFile()).getAbsolutePath() +
                    "'.");
        } else if (onlyDefaultPrivateKeyExists) {
            throw new RuntimeException("Only the private key default option ('auth.client.key') exists. " +
                    "Please add the client certificate default option ('auth.client.cert') inside the configuration file '" +
                    Objects.requireNonNull(defaultCLIProperties.getFile()).getAbsolutePath() +
                    "'.");
        } else if (defaultCertificateAndPrivateKeyExists) {
            //expected behavior
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    /**
     * The truststore loading uses a natural hierarchy to determine precedence over the security provider possibilities.
     * <ol>
     *     <li>A truststore provided as input parameter</li>
     *     <li>A server/ca certificate provided as input parameter</li>
     *     <li>A truststore provided as default option inside the cli configuration file</li>
     *     <li>A server/ca certificate provided as default option inside the cli configuration file</li>
     * </ol>
     *
     * @return a trust manager factory or null if no trust provider was configured.
     * @throws Exception if a truststore or certificate could not be accessed.
     */
    private @Nullable TrustManagerFactory buildTrustManagerFactory() throws Exception {
        final DefaultCLIProperties defaultCLIProperties =
                Objects.requireNonNull(MqttCLIMain.MQTT_CLI).defaultCLIProperties();
        final TrustManagerFactory trustManagerFactory;

        final Path defaultTruststore = defaultCLIProperties.getTruststore();
        final Path defaultServerCertificate = defaultCLIProperties.getServerCertificateChain();

        final boolean truststoreExists = clientTruststorePath != null;
        final boolean certificatesExists = serverCertificatePath != null || serverCertificateDirPath != null;
        final boolean defaultTruststoreExists = defaultTruststore != null;
        final boolean defaultCertificateExists = defaultServerCertificate != null;

        validateTrustManagerProviders(defaultCLIProperties);

        if (truststoreExists) {
            trustManagerFactory =
                    TlsUtil.createTrustManagerFactoryFromTruststore(clientTruststorePath, clientTruststorePassword);
        } else if (certificatesExists) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            final Collection<X509Certificate> certificates = new ArrayList<>();
            if (serverCertificatePath != null) {
                certificates.addAll(TlsUtil.getCertificateChainFromFile(serverCertificatePath));
            }
            if (serverCertificateDirPath != null) {
                certificates.addAll(TlsUtil.getCertificateChainFromDirectory(serverCertificateDirPath));
            }
            if (!certificates.isEmpty()) {
                keyStore.load(null, null);

                int i = 1;
                for (final X509Certificate cert : certificates) {
                    final String alias = "File_Certificate_" + i;
                    keyStore.setCertificateEntry(alias, cert);
                    i++;
                }
            }
            trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
        } else if (defaultTruststoreExists) {
            final String defaultTruststorePassword = defaultCLIProperties.getTruststorePassword();
            try {
                trustManagerFactory =
                        TlsUtil.createTrustManagerFactoryFromTruststore(defaultTruststore, defaultTruststorePassword);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default truststore ('auth.truststore') could not be loaded from '" +
                                defaultTruststore.toAbsolutePath() +
                                "'. ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
        } else if (defaultCertificateExists) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            final Collection<X509Certificate> defaultCertificates = new ArrayList<>();
            try {
                final Collection<X509Certificate> defaultCertificateChain =
                        TlsUtil.getCertificateChainFromFile(defaultServerCertificate);
                defaultCertificates.addAll(defaultCertificateChain);
            } catch (final Exception e) {
                Logger.error(e,
                        "Default server/ca certificate (auth.server.cafile) could not be loaded from'" +
                                defaultServerCertificate.toAbsolutePath() +
                                "'. ({})",
                        Throwables.getRootCause(e).getMessage());
                throw e;
            }
            if (!defaultCertificates.isEmpty()) {
                keyStore.load(null, null);

                int i = 1;
                for (final X509Certificate cert : defaultCertificates) {
                    final String alias = "File_Default_Certificate_" + i;
                    keyStore.setCertificateEntry(alias, cert);
                    i++;
                }
            }
            trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
        } else {
            trustManagerFactory = null;
        }
        return trustManagerFactory;
    }

    private void validateTrustManagerProviders(final @NotNull DefaultCLIProperties defaultCLIProperties) {
        final Path defaultTruststore = defaultCLIProperties.getTruststore();
        final Path defaultServerCertificate = defaultCLIProperties.getServerCertificateChain();

        final boolean truststoreExists = clientTruststorePath != null;
        final boolean certificatesExists = serverCertificatePath != null || serverCertificateDirPath != null;
        final boolean defaultTruststoreExists = defaultTruststore != null;
        final boolean defaultCertificateExists = defaultServerCertificate != null;

        if (truststoreExists) {
            if (certificatesExists) {
                throw new RuntimeException("The truststore parameter ('--ts', '--truststore') is used together with " +
                        "the server/ca ('--cafile', '--ca-cert', '--server-cert' or '--capath', '--ca-cert-dir', " +
                        "'--server-cert-dir') certificate parameter. " +
                        "Please only use one of the two methods.");
            }
        } else if (certificatesExists) {
            //expected behavior
            //noinspection UnnecessaryReturnStatement
            return;
        } else if (defaultTruststoreExists) {
            if (defaultCertificateExists) {
                throw new RuntimeException("The truststore default option ('auth.truststore') is used together with " +
                        "the server/ca certificate default option (auth.server.cafile). " +
                        "Please only use one of the two methods inside the configuration file '" +
                        Objects.requireNonNull(defaultCLIProperties.getFile()).getAbsolutePath() +
                        "'.");
            }
        } else if (defaultCertificateExists) {
            //expected behavior
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }


    private boolean useTls() {
        return clientKeystorePath != null ||
                clientTruststorePath != null ||
                serverCertificatePath != null ||
                serverCertificateDirPath != null ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientPrivateKeyPath != null ||
                clientCertificatePath != null ||
                useTls;
    }

    @Override
    public @NotNull String toString() {
        return "TlsOptions{" +
                "useSsl=" +
                useTls +
                ", serverCertificates=" +
                serverCertificatePath +
                ", serverCertificatesFromDir=" +
                serverCertificateDirPath +
                ", cipherSuites=" +
                cipherSuites +
                ", supportedTLSVersions=" +
                supportedTLSVersions +
                ", clientCertificates=" +
                clientCertificatePath +
                ", clientPrivateKey=" +
                clientPrivateKeyPath +
                '}';
    }
}
