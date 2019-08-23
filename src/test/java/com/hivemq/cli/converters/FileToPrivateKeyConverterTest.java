package com.hivemq.cli.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;

class FileToPrivateKeyConverterTest {

    private FileToPrivateKeyConverter fileToPrivateKeyConverter;
    private String pathToEncryptedRSAKey;
    private String pathToDecryptedRSAKey;
    private String pathToDecryptedECKey;
    private String pathToDecryptedMalformedRSAKey;

    @BeforeEach
    void setUp() {
        fileToPrivateKeyConverter = new FileToPrivateKeyConverter();

        final URL encryptedRSAKeyResource = getClass().getClassLoader().getResource("FileToPrivateKeyConverter/encrypted_RSA_key.pem");
        final URL decryptedRSAKeyResource = getClass().getClassLoader().getResource("FileToPrivateKeyConverter/decrypted_RSA_key.pem");
        final URL decryptedECKeyResource = getClass().getClassLoader().getResource("FileToPrivateKeyConverter/decrypted_EC_key.pem");
        final URL decryptedMalformedRSAKeyResource = getClass().getClassLoader().getResource("FileToPrivateKeyConverter/decrypted_malformed_RSA_key.pem");


        assertNotNull(encryptedRSAKeyResource);
        assertNotNull(decryptedRSAKeyResource);
        assertNotNull(decryptedECKeyResource);
        assertNotNull(decryptedMalformedRSAKeyResource);

        pathToEncryptedRSAKey = encryptedRSAKeyResource.getPath();
        pathToDecryptedRSAKey = decryptedRSAKeyResource.getPath();
        pathToDecryptedECKey = decryptedECKeyResource.getPath();
        pathToDecryptedMalformedRSAKey = decryptedMalformedRSAKeyResource.getPath();

    }

    @Test
    void convert_ENCRYPTED_RSA_KEY_SUCCESS() throws Exception {
        System.setIn(new ByteArrayInputStream("password".getBytes()));
        PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToEncryptedRSAKey);
        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_RSA_KEY_SUCCESS() throws Exception {
        PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToDecryptedRSAKey);
        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_EC_KEY_SUCCESS() throws Exception {
        PrivateKey privateKey = fileToPrivateKeyConverter.convert(pathToDecryptedECKey);
        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_MALFORMED_RSA_KEY_FAILURE() throws Exception {
        Exception e = assertThrows(Exception.class, () -> fileToPrivateKeyConverter.convert(pathToDecryptedMalformedRSAKey));
        assertEquals(FileToPrivateKeyConverter.MALFORMED_KEY, e.getMessage());
    }

    @Test
    void convert_ENCRYPTED_RSA_KEY_WRONG_PASSWORD_FAILURE() throws Exception {
        System.setIn(new ByteArrayInputStream("badpassword".getBytes()));
        Exception e = assertThrows(Exception.class, () -> fileToPrivateKeyConverter.convert(pathToEncryptedRSAKey));
        assertEquals(FileToPrivateKeyConverter.WRONG_PASSWORD, e.getMessage());
    }

}