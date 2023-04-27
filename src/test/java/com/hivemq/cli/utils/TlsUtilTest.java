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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TlsUtilTest {

    private @NotNull Path pathToValidCertificate;
    private @NotNull Path pathToInvalidFileExtensionCertificate;
    private @NotNull Path pathToNoFileExtensionCertificate;
    private @NotNull Path pathToInvalidCertificate;
    private @NotNull Path pathToValidDirectory;
    private @NotNull Path pathToDirectoryWithoutCertificates;
    private @NotNull Path pathToEncryptedRSAKey;
    private @NotNull Path pathToDecryptedRSAKey;
    private @NotNull Path pathToDecryptedECKey;
    private @NotNull Path pathToDecryptedMalformedRSAKey;

    @BeforeEach
    void setUp() throws URISyntaxException {
        final URL validCertificateResource = getClass().getResource("/TlsUtil/validCertificate.pem");
        final URL invalidFileExtensionResource = getClass().getResource("/TlsUtil/invalidFileExtensionCertificate.der");
        final URL noFileExtensionCertificate = getClass().getResource("/TlsUtil/noFileExtensionCertificate");
        final URL invalidCertificate = getClass().getResource("/TlsUtil/invalidCertificate.pem");
        final URL validCertificateDirResource = getClass().getResource("/TlsUtil/directory_with_certificates");
        final URL noCertificatesDir = getClass().getResource("/TlsUtil/directory_without_certificates");
        final URL encryptedRSAKeyResource = getClass().getResource("/TlsUtil/encrypted_RSA_key.pem");
        final URL decryptedRSAKeyResource = getClass().getResource("/TlsUtil/decrypted_RSA_key.pem");
        final URL decryptedECKeyResource = getClass().getResource("/TlsUtil/decrypted_EC_key.pem");
        final URL decryptedMalformedRSAKeyResource = getClass().getResource("/TlsUtil/decrypted_malformed_RSA_key.pem");

        assertNotNull(validCertificateResource);
        assertNotNull(invalidFileExtensionResource);
        assertNotNull(noFileExtensionCertificate);
        assertNotNull(invalidCertificate);
        assertNotNull(validCertificateDirResource);
        assertNotNull(validCertificateResource);
        assertNotNull(noCertificatesDir);
        assertNotNull(encryptedRSAKeyResource);
        assertNotNull(decryptedRSAKeyResource);
        assertNotNull(decryptedECKeyResource);
        assertNotNull(decryptedMalformedRSAKeyResource);

        pathToValidCertificate = Paths.get(validCertificateResource.toURI());
        pathToInvalidFileExtensionCertificate = Paths.get(invalidFileExtensionResource.toURI());
        pathToNoFileExtensionCertificate = Paths.get(noFileExtensionCertificate.toURI());
        pathToInvalidCertificate = Paths.get(invalidCertificate.toURI());
        pathToValidDirectory = Paths.get(validCertificateDirResource.toURI());
        pathToDirectoryWithoutCertificates = Paths.get(noCertificatesDir.toURI());
        pathToEncryptedRSAKey = Paths.get(encryptedRSAKeyResource.toURI());
        pathToDecryptedRSAKey = Paths.get(decryptedRSAKeyResource.toURI());
        pathToDecryptedECKey = Paths.get(decryptedECKeyResource.toURI());
        pathToDecryptedMalformedRSAKey = Paths.get(decryptedMalformedRSAKeyResource.toURI());
    }

    @Test
    void convertSuccess() throws Exception {
        final Collection<X509Certificate> x509Certificates =
                TlsUtil.getCertificateChainFromFile(pathToValidCertificate);

        assertEquals(1, x509Certificates.size());
    }

    @Test
    void convert_FileNotFound() {
        final Exception e = assertThrows(FileNotFoundException.class,
                () -> TlsUtil.getCertificateChainFromFile(Paths.get("wrongPathXYZ.pem")));
        assertEquals(FileUtil.FILE_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_InvalidFileExtensionCertificate() {
        final Exception e = assertThrows(Exception.class,
                () -> TlsUtil.getCertificateChainFromFile(pathToInvalidFileExtensionCertificate));
        assertEquals(TlsUtil.NO_VALID_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_NoFileExtensionCertificate() {
        final Exception e = assertThrows(Exception.class,
                () -> TlsUtil.getCertificateChainFromFile(pathToNoFileExtensionCertificate));
        assertEquals(TlsUtil.NO_VALID_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_InvalidCertificate() {
        final Exception e =
                assertThrows(Exception.class, () -> TlsUtil.getCertificateChainFromFile(pathToInvalidCertificate));
        assertEquals(TlsUtil.NO_VALID_CERTIFICATE, e.getMessage());
    }

    @Test
    void convert_Success() throws Exception {
        final Collection<X509Certificate> certificates = TlsUtil.getCertificateChainFromDirectory(pathToValidDirectory);

        assertNotNull(certificates);
        assertEquals(3, certificates.size());
    }

    @Test
    void convert_Failure_DirectoryNotFound() {
        final Exception e = assertThrows(FileNotFoundException.class,
                () -> TlsUtil.getCertificateChainFromDirectory(Paths.get("invalidPath")));
        assertEquals(TlsUtil.DIRECTORY_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_Failure_DirectoryIsAFile() {
        final Exception e =
                assertThrows(Exception.class, () -> TlsUtil.getCertificateChainFromDirectory(pathToValidCertificate));
        assertEquals(TlsUtil.NOT_A_DIRECTORY, e.getMessage());
    }

    @Test
    void convert_Failure_DirectoryWithoutCertificates() {
        final Exception e = assertThrows(Exception.class,
                () -> TlsUtil.getCertificateChainFromDirectory(pathToDirectoryWithoutCertificates));
        assertEquals(TlsUtil.NO_CERTIFICATES_FOUND_IN_DIRECTORY, e.getMessage());
    }

    @Test
    void generateX509Certificate_Success() throws Exception {
        final Collection<X509Certificate> x509Certificates =
                TlsUtil.generateX509Certificates(new File(pathToValidCertificate.toUri()));

        assertEquals(1, x509Certificates.size());
    }

    @Test
    void generateX509Certificate_Failure() {
        final Exception e = assertThrows(CertificateException.class,
                () -> TlsUtil.generateX509Certificates(new File(pathToInvalidCertificate.toUri())));
        assertEquals(TlsUtil.NO_VALID_CERTIFICATE, e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test.pem", "test.cer", "test.crt", "pem.pem", "cert.cer", "crt.crt", "pem.cert.crt", "cer.crt"})
    void endsWithValidExtension_Success(final @NotNull String fileName) {
        assertTrue(TlsUtil.isCertificate(fileName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test.pe", "test.cr", "test.cert", "cer.ctr", "", " ", "pem", "crt", "cer", "pempem", "test.pem.ext"})
    void endsWithValidExtension_Failure(final @NotNull String fileName) {
        assertFalse(TlsUtil.isCertificate(fileName));
    }

    @Test
    void convert_ENCRYPTED_RSA_KEY_SUCCESS() throws Exception {
        System.setIn(new ByteArrayInputStream("password".getBytes()));
        final PrivateKey privateKey = TlsUtil.getPrivateKeyFromFile(pathToEncryptedRSAKey, null);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_RSA_KEY_SUCCESS() throws Exception {
        final PrivateKey privateKey = TlsUtil.getPrivateKeyFromFile(pathToDecryptedRSAKey, null);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_EC_KEY_SUCCESS() throws Exception {
        final PrivateKey privateKey = TlsUtil.getPrivateKeyFromFile(pathToDecryptedECKey, null);

        assertNotNull(privateKey);
    }

    @Test
    void convert_DECRYPTED_MALFORMED_RSA_KEY_FAILURE() {
        final Exception e = assertThrows(Exception.class,
                () -> TlsUtil.getPrivateKeyFromFile(pathToDecryptedMalformedRSAKey, null));
        assertEquals(TlsUtil.MALFORMED_PRIVATE_KEY, e.getMessage());
    }
}
