package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

class CertificateConverterUtils {
    static final String[] FILE_EXTENSIONS = {".pem", ".cer", ".crt"};
    static final String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certficate,";

    static X509Certificate generateX509Certificate(final @NotNull File keyFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try {
            return (X509Certificate) cf.generateCertificate(new FileInputStream(keyFile));
        } catch (CertificateException | FileNotFoundException ce) {
            throw new CertificateException(NO_VALID_CERTIFICATE);
        }
    }

    static boolean endsWithValidExtension(final @NotNull String fileName) {
        for (String extension : FILE_EXTENSIONS) {
            if (fileName.endsWith(extension)) return true;
        }
        return false;
    }
}
