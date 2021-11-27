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

import com.hivemq.cli.utils.CertificateConverterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileToCertificatesConverterTest {

    private FileToCertificatesConverter fileToCertificatesConverter;
    private String pathToValidCertificate;
    private String pathToInvalidFileExtensionCertificate;
    private String pathToNoFileExtensionCertificate;
    private String pathToInvalidCertificate;

    @BeforeEach
    void setUp() {
        fileToCertificatesConverter = new FileToCertificatesConverter();
        final URL validCertificateResource = getClass().getResource("/FileToCertificateConverter/validCertificate.pem");
        final URL invalidFileExtensionResource = getClass().getResource("/FileToCertificateConverter/invalidFileExtensionCertificate.der");
        final URL noFileExtensionCertificate = getClass().getResource("/FileToCertificateConverter/noFileExtensionCertificate");
        final URL invalidCertificate = getClass().getResource("/FileToCertificateConverter/invalidCertificate.pem");

        assert validCertificateResource != null;
        assert invalidFileExtensionResource != null;
        assert noFileExtensionCertificate != null;
        assert invalidCertificate != null;

        pathToValidCertificate = validCertificateResource.getPath();
        pathToInvalidFileExtensionCertificate = invalidFileExtensionResource.getPath();
        pathToNoFileExtensionCertificate = noFileExtensionCertificate.getPath();
        pathToInvalidCertificate = invalidCertificate.getPath();
    }

    @Test
    void convertSuccess() throws Exception {
        final Collection<X509Certificate> x509Certificates = fileToCertificatesConverter.convert(pathToValidCertificate);

        assertEquals(1, x509Certificates.size());
    }

    @Test
    void convert_FileNotFound() {
        Exception e = assertThrows(FileNotFoundException.class, () -> fileToCertificatesConverter.convert("wrongPathXYZ.pem"));
        assertEquals(FileConverter.FILE_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_InvalidFileExtensionCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificatesConverter.convert(pathToInvalidFileExtensionCertificate));
        assertEquals(FileToCertificatesConverter.NO_VALID_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_NoFileExtensionCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificatesConverter.convert(pathToNoFileExtensionCertificate));
        assertEquals(FileToCertificatesConverter.NO_VALID_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_InvalidCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificatesConverter.convert(pathToInvalidCertificate));
        assertEquals(CertificateConverterUtils.NO_VALID_CERTIFICATE, e.getMessage());
    }
}