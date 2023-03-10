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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class TlsUtil {

    static final @NotNull String @NotNull [] CERTIFICATE_FILE_EXTENSION = {".pem", ".cer", ".crt"};
    static final @NotNull String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certificate.";

    static final @NotNull String UNRECOGNIZED_PRIVATE_KEY = "The private key could not be recognized.";
    static final @NotNull String MALFORMED_PRIVATE_KEY = "The private key could not be read.";
    static final @NotNull String NO_VALID_FILE_EXTENSION =
            "The given file does not conform to a valid Certificate File Extension as " +
                    Arrays.toString(CERTIFICATE_FILE_EXTENSION);

    static final @NotNull String DIRECTORY_NOT_FOUND = "The given directory was not found.";
    static final @NotNull String NOT_A_DIRECTORY = "The given path is not a valid directory";
    static final @NotNull String NO_CERTIFICATES_FOUND_IN_DIRECTORY =
            "The given directory does not contain any valid certificates";

    public @NotNull PrivateKey getPrivateKeyFromFile(
            final @NotNull Path privateKeyPath, final @Nullable String privateKeyPassword) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // read the keyfile
        final File keyFile = FileUtil.convert(privateKeyPath);
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
            //TODO: get DER file format working
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

    public @NotNull Collection<X509Certificate> getCertificateChainFromFile(final @NotNull Path certificatePath)
            throws Exception {
        final File certificateFile = FileUtil.convert(certificatePath);

        final boolean correctExtension = endsWithValidExtension(certificateFile.getName());

        if (!correctExtension) {
            throw new IllegalArgumentException(NO_VALID_FILE_EXTENSION);
        }

        return generateX509Certificates(certificateFile);
    }

    public @NotNull Collection<X509Certificate> getCertificateChainFromDirectory(final @NotNull Path certificateDirectory)
            throws Exception {
        final File directory = new File(certificateDirectory.toUri());

        if (!directory.exists()) {
            throw new FileNotFoundException(DIRECTORY_NOT_FOUND);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(NOT_A_DIRECTORY);
        }

        final File[] validFiles = directory.listFiles((dir, name) -> endsWithValidExtension(name));

        if (validFiles == null || validFiles.length == 0) {
            throw new IllegalArgumentException(NO_CERTIFICATES_FOUND_IN_DIRECTORY);
        }

        final Collection<X509Certificate> certificates = new ArrayList<>();

        for (final File validFile : validFiles) {
            certificates.addAll(generateX509Certificates(validFile));
        }

        return certificates;
    }

    public static @NotNull Collection<X509Certificate> generateX509Certificates(final @NotNull File keyFile)
            throws Exception {
        // Instantiate X509 certificate factory
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");

        try {
            // Parse X509 certificate chain
            final Collection<? extends Certificate> certificateChainCollection =
                    cf.generateCertificates(new FileInputStream(keyFile));

            // Cast to X509Certificate collection and return it
            //noinspection unchecked
            return (Collection<X509Certificate>) certificateChainCollection;

        } catch (final CertificateException | FileNotFoundException e) {
            throw new CertificateException(NO_VALID_CERTIFICATE);
        }
    }

    public static boolean endsWithValidExtension(final @NotNull String fileName) {
        for (final String extension : CERTIFICATE_FILE_EXTENSION) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPKCS12Keystore(final @NotNull Path keystorePath) {
        boolean isPkcs12 = false;
        for (final String pkcs12Ending : new String[]{".p12", ".pfx"}) {
            isPkcs12 |= keystorePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(pkcs12Ending);
        }
        return isPkcs12;
    }

    public static boolean isJKSKeystore(final @NotNull Path keystorePath) {
        return keystorePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("jks");
    }
}
