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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DirectoryToCertificatesConverterTest {
    private DirectoryToCertificatesConverter directoryToCertificatesConverter;
    private String pathToValidDirectory;
    private String pathToValidCertificate;
    private String pathToDirectoryWithoutCertificates;

    @BeforeEach
    void setUp() {
        directoryToCertificatesConverter = new DirectoryToCertificatesConverter();
        final URL validCertificateDirResource = getClass().getClassLoader().getResource("FileToCertificateConverter/directory_with_certificates");
        final URL noCertificatesDir = getClass().getClassLoader().getResource("FileToCertificateConverter/directory_without_certificates");
        final URL validCertificateResource = getClass().getClassLoader().getResource("FileToCertificateConverter/validCertificate.pem");

        assertNotNull(validCertificateDirResource);
        assertNotNull(validCertificateResource);
        assertNotNull(noCertificatesDir);

        pathToValidDirectory = validCertificateDirResource.getPath();
        pathToValidCertificate = validCertificateResource.getPath();
        pathToDirectoryWithoutCertificates = noCertificatesDir.getPath();
    }

    @Test
    void convert_Success() throws Exception {
        Collection<X509Certificate> certificates = directoryToCertificatesConverter.convert(pathToValidDirectory);
        assertNotNull(certificates);
        assertEquals(3, certificates.size());
    }

    @Test
    void convert_Failure_DirectoryNotFound() {
        Exception e = assertThrows(FileNotFoundException.class, () -> directoryToCertificatesConverter.convert("invalidPath"));
        assertEquals(DirectoryToCertificatesConverter.DIRECTORY_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_Failure_DirectoryIsAFile() {
        Exception e = assertThrows(Exception.class, () -> directoryToCertificatesConverter.convert(pathToValidCertificate));
        assertEquals(DirectoryToCertificatesConverter.NOT_A_DIRECTORY, e.getMessage());
    }


    @Test
    void convert_Failure_DirectoryWithoutCertificates() {
        Exception e = assertThrows(Exception.class, () -> directoryToCertificatesConverter.convert(pathToDirectoryWithoutCertificates));
        assertEquals(DirectoryToCertificatesConverter.NO_CERTIFICATES_FOUND_IN_DIRECTORY, e.getMessage());
    }
}