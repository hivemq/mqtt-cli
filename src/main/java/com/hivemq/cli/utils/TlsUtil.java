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

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class TlsUtil {

    private TlsUtil() {

    }

    static final @NotNull String JKS_FILE_EXTENSION = "jks";
    static final @NotNull String @NotNull [] PKCS12_FILE_EXTENSIONS = {".p12", "pfx"};
    static final @NotNull String @NotNull [] CERTIFICATE_FILE_EXTENSIONS = {".pem", ".cer", ".crt"};
    static final @NotNull String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certificate.";

    static final @NotNull String UNRECOGNIZED_PRIVATE_KEY = "The private key could not be recognized.";
    static final @NotNull String MALFORMED_PRIVATE_KEY = "The private key could not be read.";
    static final @NotNull String NO_VALID_FILE_EXTENSION =
            "The given file does not conform to a valid Certificate File Extension as " +
                    Arrays.toString(CERTIFICATE_FILE_EXTENSIONS);

    static final @NotNull String DIRECTORY_NOT_FOUND = "The given directory was not found.";
    static final @NotNull String NOT_A_DIRECTORY = "The given path is not a valid directory";
    static final @NotNull String NO_CERTIFICATES_FOUND_IN_DIRECTORY =
            "The given directory does not contain any valid certificates";

    public static @NotNull KeyManagerFactory createKeyManagerFactoryFromKeystore(
            final @NotNull Path clientKeystore,
            final @Nullable String clientKeystorePassword,
            final @Nullable String clientKeystorePrivateKeyPassword) throws Exception {
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
            throw new KeyStoreException(
                    "Unknown keystore type. Please use a PKCS#12 (.p12/.pfx) or JKS (.jks) keystore.");
        }
        return keyManagerFactory;
    }

    public static @NotNull TrustManagerFactory createTrustManagerFactoryFromTruststore(
            final @NotNull Path clientTruststore, final @Nullable String clientTruststorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final TrustManagerFactory trustManagerFactory;
        final KeyStore keyStore;
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
            try (final InputStream inputStream = Files.newInputStream(clientTruststore, StandardOpenOption.READ)) {
                keyStore.load(inputStream, truststorePassword);
            }
        } else {
            throw new KeyStoreException(
                    "Unknown truststore type. Please use a PKCS#12 (.p12/.pfx) or JKS (.jks) truststore.");
        }
        trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }

    public static @NotNull PrivateKey getPrivateKeyFromFile(
            final @NotNull Path privateKeyPath, final @Nullable String privateKeyPassword) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // read the keyfile
        final File keyFile = FileUtil.assertFileExists(privateKeyPath);
        final PEMParser pemParser = new PEMParser(new FileReader(keyFile));

        final Object object;
        try {
            object = pemParser.readObject();
        } catch (final PEMException pe) {
            throw new Exception(MALFORMED_PRIVATE_KEY);
        }

        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        final PrivateKey privateKey;
        if (object == null) {
            throw new IllegalArgumentException("KEY IS NULL");
            //TODO: get DER file format working. Different ticket
            /*
            final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Files.readAllBytes(keyFile.toPath()));
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            final PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pkcs8KeySpec.getEncoded());
            final RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded()));

            final PKCS8EncryptedPrivateKeyInfo newObject =
                    new PKCS8EncryptedPrivateKeyInfo(Files.readAllBytes(keyFile.toPath()));
            final char[] password = PasswordUtils.readPassword("Enter private key password:");
            final InputDecryptorProvider decryptorProvider =
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
            final PrivateKeyInfo privateKeyInfo = newObject.decryptPrivateKeyInfo(decryptorProvider);
            return converter.getPrivateKey(privateKeyInfo);
            */
        } else if (object instanceof PEMEncryptedKeyPair) {
            final char[] password;
            if (privateKeyPassword != null) {
                password = privateKeyPassword.toCharArray();
            } else {
                password = PasswordUtils.readPassword("Enter private key password: ");
            }
            final PEMEncryptedKeyPair encryptedPrivateKey = (PEMEncryptedKeyPair) object;
            final PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder().build(password);
            final KeyPair keyPair = converter.getKeyPair(encryptedPrivateKey.decryptKeyPair(decryptorProvider));
            privateKey = keyPair.getPrivate();
        } else if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            final char[] password;
            if (privateKeyPassword != null) {
                password = privateKeyPassword.toCharArray();
            } else {
                password = PasswordUtils.readPassword("Enter private key password: ");
            }
            final PKCS8EncryptedPrivateKeyInfo encryptedPrivateKey = (PKCS8EncryptedPrivateKeyInfo) object;
            final InputDecryptorProvider decryptorProvider =
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);
            final PrivateKeyInfo privateKeyInfo = encryptedPrivateKey.decryptPrivateKeyInfo(decryptorProvider);
            privateKey = converter.getPrivateKey(privateKeyInfo);
        } else if (object instanceof PEMKeyPair) {
            privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
        } else if (object instanceof PrivateKeyInfo) {
            privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
        } else {
            throw new IllegalArgumentException(UNRECOGNIZED_PRIVATE_KEY);
        }
        return privateKey;
    }

    public static @NotNull Collection<X509Certificate> getCertificateChainFromFile(final @NotNull Path certificatePath)
            throws Exception {
        final File certificateFile = FileUtil.assertFileExists(certificatePath);
        final boolean isCertificate = isCertificate(certificateFile.getName());

        if (!isCertificate) {
            throw new IllegalArgumentException(NO_VALID_FILE_EXTENSION);
        }
        return generateX509Certificates(certificateFile);
    }

    public static @NotNull Collection<X509Certificate> getCertificateChainFromDirectory(final @NotNull Path certificateDirectory)
            throws Exception {
        final File directory = new File(certificateDirectory.toUri());

        if (!directory.exists()) {
            throw new FileNotFoundException(DIRECTORY_NOT_FOUND);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(NOT_A_DIRECTORY);
        }

        final File[] validFiles = directory.listFiles((dir, name) -> isCertificate(name));
        if (validFiles == null || validFiles.length == 0) {
            throw new IllegalArgumentException(NO_CERTIFICATES_FOUND_IN_DIRECTORY);
        }

        final Collection<X509Certificate> certificates = new ArrayList<>();
        for (final File validFile : validFiles) {
            certificates.addAll(generateX509Certificates(validFile));
        }
        return certificates;
    }

    @VisibleForTesting
    static @NotNull Collection<X509Certificate> generateX509Certificates(final @NotNull File keyFile) throws Exception {
        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        try {
            final Collection<? extends Certificate> certificateChainCollection =
                    certificateFactory.generateCertificates(new FileInputStream(keyFile));
            //noinspection unchecked
            return (Collection<X509Certificate>) certificateChainCollection;
        } catch (final CertificateException | FileNotFoundException e) {
            throw new CertificateException(NO_VALID_CERTIFICATE);
        }
    }

    static boolean isCertificate(final @NotNull String fileName) {
        for (final String certificateEnding : CERTIFICATE_FILE_EXTENSIONS) {
            if (fileName.endsWith(certificateEnding)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPKCS12Keystore(final @NotNull Path keystorePath) {
        boolean isPkcs12 = false;
        for (final String pkcs12Ending : PKCS12_FILE_EXTENSIONS) {
            isPkcs12 |= keystorePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(pkcs12Ending);
        }
        return isPkcs12;
    }

    public static boolean isJKSKeystore(final @NotNull Path keystorePath) {
        return keystorePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(JKS_FILE_EXTENSION);
    }
}
