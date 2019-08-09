package com.hivemq.cli.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryToCertificateCollectionConverterTest {
    private DirectoryToCertificateCollectionConverter directoryToCertificateCollectionConverter;
    private String pathToValidDirectory;
    private String pathToValidCertificate;
    private String pathToDirectoryWithoutCertificates;

    @BeforeEach
    void setUp() {
        directoryToCertificateCollectionConverter = new DirectoryToCertificateCollectionConverter();
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
        Collection<X509Certificate> certificates = directoryToCertificateCollectionConverter.convert(pathToValidDirectory);
        assertNotNull(certificates);
        assertEquals(3, certificates.size());
    }

    @Test
    void convert_Failure_DirectoryNotFound() {
        Exception e = assertThrows(FileNotFoundException.class, () -> directoryToCertificateCollectionConverter.convert("invalidPath"));
        assertEquals(DirectoryToCertificateCollectionConverter.DIRECTORY_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_Failure_DirectoryIsAFile() {
        Exception e = assertThrows(Exception.class, () -> directoryToCertificateCollectionConverter.convert(pathToValidCertificate));
        assertEquals(DirectoryToCertificateCollectionConverter.NOT_A_DIRECTORY, e.getMessage());
    }


    @Test
    void convert_Failure_DirectoryWithoutCertificates() {
        Exception e = assertThrows(Exception.class, () -> directoryToCertificateCollectionConverter.convert(pathToDirectoryWithoutCertificates));
        assertEquals(DirectoryToCertificateCollectionConverter.NO_CERTIFICATES_FOUND_IN_DIRECTORY, e.getMessage());
    }
}