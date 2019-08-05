package com.hivemq.cli.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URL;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileToCertificateConverterTest {

    private FileToCertificateConverter fileToCertificateConverter;
    private String pathToValidCertficate;
    private String pathToInvalidFileExtensionCertificate;
    private String pathToNoFileExtensionCertificate;
    private String pathToInvalidCertificate;

    @BeforeEach
    void setUp() {
        fileToCertificateConverter = new FileToCertificateConverter();
        final URL validCertificateResource = getClass().getClassLoader().getResource("FileToCertificateConverter/validCertificate.pem");
        final URL invalidFileExtensionResource = getClass().getClassLoader().getResource("FileToCertificateConverter/invalidFileExtensionCertificate.der");
        final URL noFileExtensionCertificate = getClass().getClassLoader().getResource("FileToCertificateConverter/noFileExtensionCertificate");
        final URL invalidCertificate = getClass().getClassLoader().getResource("FileToCertificateConverter/invalidCertificate.pem");

        assert validCertificateResource != null;
        assert invalidFileExtensionResource != null;
        assert noFileExtensionCertificate != null;
        assert invalidCertificate != null;

        pathToValidCertficate = validCertificateResource.getPath();
        pathToInvalidFileExtensionCertificate = invalidFileExtensionResource.getPath();
        pathToNoFileExtensionCertificate = noFileExtensionCertificate.getPath();
        pathToInvalidCertificate = invalidCertificate.getPath();
    }

    @Test
    void convertSuccess() throws Exception {
        X509Certificate cert = fileToCertificateConverter.convert(pathToValidCertficate);
        cert.checkValidity();
    }

    @Test
    void convert_FileNotFound() {
        Exception e = assertThrows(FileNotFoundException.class, () -> fileToCertificateConverter.convert("wrongPathXYZ.pem"));
        assertEquals(FileToCertificateConverter.FILE_NOT_FOUND, e.getMessage());
    }

    @Test
    void convert_InvalidFileExtensionCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificateConverter.convert(pathToInvalidFileExtensionCertificate));
        assertEquals(FileToCertificateConverter.NO_VALID_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_NoFileExtensionCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificateConverter.convert(pathToNoFileExtensionCertificate));
        assertEquals(FileToCertificateConverter.MISSING_FILE_EXTENSION, e.getMessage());
    }

    @Test
    void convert_InvalidCertificate() {
        Exception e = assertThrows(Exception.class, () -> fileToCertificateConverter.convert(pathToInvalidCertificate));
        assertEquals(FileToCertificateConverter.NO_VALID_CERTIFICATE, e.getMessage());
    }
}