package com.hivemq.cli.converters;


import com.hivemq.cli.utils.PasswordUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.security.PrivateKey;
import java.security.Security;

public class FileToPrivateKeyConverter implements CommandLine.ITypeConverter<PrivateKey> {
    static final String UNRECOGNIZED_KEY = "The private key could not be recognized.";
    static final String MALFORMED_KEY = "The private key could not be read.";
    static final String WRONG_PASSWORD = "The given password to decrypt the private key was wrong.";

    @Override
    public PrivateKey convert(final @NotNull String s) throws Exception {
        final File keyFile = new File(s);
        return getPrivateKeyFromFile(keyFile);
    }

    private PrivateKey getPrivateKeyFromFile(final @NotNull File keyFile) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // read the keyfile
        final PEMParser pemParser = new PEMParser(new FileReader(keyFile));

        Object object;
        try {
            object = pemParser.readObject();
        } catch (PEMException pe) {
            Logger.debug(pe);
            throw new Exception(MALFORMED_KEY);
        }

        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        PrivateKey privateKey;
        if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            // if encrypted key is password protected - decrypt it with given password

            char[] password = PasswordUtils.readPassword("Enter private key password:");

            final PKCS8EncryptedPrivateKeyInfo encryptedPrivateKey = (PKCS8EncryptedPrivateKeyInfo) object;
            final InputDecryptorProvider decryptorProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password);

            PrivateKeyInfo privateKeyInfo;
            try {
                privateKeyInfo = encryptedPrivateKey.decryptPrivateKeyInfo(decryptorProvider);
            } catch (PKCSException pkcse) {
                throw new IllegalArgumentException(WRONG_PASSWORD);
            }

            privateKey = converter.getPrivateKey(privateKeyInfo);
        } else if (object instanceof PEMKeyPair) {
            // if key pair is already decrypted
            privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
        } else {
            throw new IllegalArgumentException(UNRECOGNIZED_KEY);
        }

        // Convert extracted private key into native java Private key
        return privateKey;
    }
}
