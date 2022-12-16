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

package com.hivemq.cli.converters;

import com.hivemq.cli.utils.PasswordUtils;
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
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;

public class FileToPrivateKeyConverter implements CommandLine.ITypeConverter<PrivateKey> {

    static final @NotNull String UNRECOGNIZED_KEY = "The private key could not be recognized.";
    static final @NotNull String MALFORMED_KEY = "The private key could not be read.";

    @Override
    public @NotNull PrivateKey convert(final @NotNull String s) throws Exception {
        final FileConverter fileConverter = new FileConverter();
        final File keyFile = fileConverter.convert(s);
        return getPrivateKeyFromFile(keyFile);
    }

    private @NotNull PrivateKey getPrivateKeyFromFile(final @NotNull File keyFile) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        // read the keyfile
        final PEMParser pemParser = new PEMParser(new FileReader(keyFile));

        final Object object;
        try {
            object = pemParser.readObject();
        } catch (final PEMException pe) {
            throw new Exception(MALFORMED_KEY);
        }

        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        final PrivateKey privateKey;
        if (object instanceof PEMEncryptedKeyPair) {
            final char[] password = PasswordUtils.readPassword("Enter private key password: ");
            final PEMEncryptedKeyPair encryptedPrivateKey = (PEMEncryptedKeyPair) object;
            final PEMDecryptorProvider decryptorProvider = new JcePEMDecryptorProviderBuilder().build(password);
            final KeyPair keyPair = converter.getKeyPair(encryptedPrivateKey.decryptKeyPair(decryptorProvider));
            privateKey = keyPair.getPrivate();
        } else if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            final char[] password = PasswordUtils.readPassword("Enter private key password:");
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
            throw new IllegalArgumentException(UNRECOGNIZED_KEY);
        }

        return privateKey;
    }
}
