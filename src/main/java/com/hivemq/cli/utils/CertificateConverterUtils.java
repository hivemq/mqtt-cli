package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateConverterUtils {
    public static final String[] FILE_EXTENSIONS = {".pem", ".cer", ".crt"};
    public static final String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certficate,";

    public static X509Certificate generateX509Certificate(final @NotNull File keyFile) throws Exception {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try {
            return (X509Certificate) cf.generateCertificate(new FileInputStream(keyFile));
        } catch (CertificateException | FileNotFoundException ce) {
            throw new CertificateException(NO_VALID_CERTIFICATE);
        }
    }

    public static boolean endsWithValidExtension(final @NotNull String fileName) {
        for (final String extension : FILE_EXTENSIONS) {
            if (fileName.endsWith(extension)) return true;
        }
        return false;
    }
}
