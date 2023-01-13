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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileToPrivateKeyConverterTest {

    private @NotNull FileToPrivateKeyConverter fileToPrivateKeyConverter;
    private @NotNull String pathToEncryptedRSAKey;
    private @NotNull String pathToDecryptedRSAKey;
    private @NotNull String pathToDecryptedECKey;
    private @NotNull String pathToDecryptedMalformedRSAKey;

    @BeforeEach
    void setUp() throws URISyntaxException {
        fileToPrivateKeyConverter = new FileToPrivateKeyConverter();
        final URL encryptedRSAKeyResource = getClass().getResource("/FileToPrivateKeyConverter/encrypted_RSA_key.pem");
        final URL decryptedRSAKeyResource = getClass().getResource("/FileToPrivateKeyConverter/decrypted_RSA_key.pem");
        final URL decryptedECKeyResource = getClass().getResource("/FileToPrivateKeyConverter/decrypted_EC_key.pem");
        final URL decryptedMalformedRSAKeyResource =
                getClass().getResource("/FileToPrivateKeyConverter/decrypted_malformed_RSA_key.pem");

        assertNotNull(encryptedRSAKeyResource);
        assertNotNull(decryptedRSAKeyResource);
        assertNotNull(decryptedECKeyResource);
        assertNotNull(decryptedMalformedRSAKeyResource);

        pathToEncryptedRSAKey = Paths.get(encryptedRSAKeyResource.toURI()).toString();
        pathToDecryptedRSAKey = Paths.get(decryptedRSAKeyResource.toURI()).toString();
        pathToDecryptedECKey = Paths.get(decryptedECKeyResource.toURI()).toString();
        pathToDecryptedMalformedRSAKey = Paths.get(decryptedMalformedRSAKeyResource.toURI()).toString();

    }

    @Test
    void convert_ENCRYPTED_RSA_KEY_SUCCESS() throws Exception {
        System.setIn(new ByteArrayInputStream("password".getBytes()));
        final PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToEncryptedRSAKey);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_RSA_KEY_SUCCESS() throws Exception {
        final PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToDecryptedRSAKey);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_EC_KEY_SUCCESS() throws Exception {
        final PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToDecryptedECKey);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_MALFORMED_RSA_KEY_FAILURE() {
        final Exception e =
                assertThrows(Exception.class, () -> fileToPrivateKeyConverter.convert(pathToDecryptedMalformedRSAKey));
        assertEquals(FileToPrivateKeyConverter.MALFORMED_KEY, e.getMessage());
    }
}
