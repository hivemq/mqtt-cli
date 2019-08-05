package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class FileToCertificateConverter implements CommandLine.ITypeConverter<X509Certificate> {
    static final String[] FILE_EXTENSIONS = {"pem", "cer", "crt"};
    static final String NO_VALID_FILE_EXTENSION = "The given file does not conform to a valid Certificate File Extension as " + Arrays.toString(FILE_EXTENSIONS);
    static final String MISSING_FILE_EXTENSION = "The given file is missing a Certificate File Extensions as " + Arrays.toString(FILE_EXTENSIONS);
    static final String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certficate,";
    static final String FILE_NOT_FOUND = "The given certificate file was not found.";

    @Override
    public X509Certificate convert(final @NotNull String s) throws Exception {
        File keyFile = new File(s);

        if (!keyFile.exists())
            throw new FileNotFoundException(FILE_NOT_FOUND);

        checkForRightFileExtension(keyFile);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try {
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(new FileInputStream(keyFile));
            return certificate;
        } catch (CertificateException ce) {
            throw new Exception(NO_VALID_CERTIFICATE);
        }

    }

    private void checkForRightFileExtension(final @NotNull File certficateFile) throws Exception {
        String fileName = certficateFile.getName();
        int extensionDotIndex = fileName.lastIndexOf('.');

        if (extensionDotIndex == -1 || extensionDotIndex == 0)
            throw new Exception(MISSING_FILE_EXTENSION);

        String fileNameExtension = fileName.substring(extensionDotIndex + 1);

        if (!Arrays.asList(FILE_EXTENSIONS).contains(fileNameExtension))
            throw new Exception(NO_VALID_FILE_EXTENSION);
    }

}
