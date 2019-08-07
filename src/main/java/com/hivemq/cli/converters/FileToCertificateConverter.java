package com.hivemq.cli.converters;

import com.hivemq.cli.utils.CertificateConverterUtils;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class FileToCertificateConverter implements CommandLine.ITypeConverter<X509Certificate> {


    static final String FILE_NOT_FOUND = "The given certificate file was not found.";
    static final String NO_VALID_FILE_EXTENSION = "The given file does not conform to a valid Certificate File Extension as " + Arrays.toString(CertificateConverterUtils.FILE_EXTENSIONS);
    static final String NOT_A_FILE = "The given path is not a valid file.";

    @Override
    public X509Certificate convert(final @NotNull String s) throws Exception {
        File keyFile = new File(s);

        if (!keyFile.exists())
            throw new FileNotFoundException(FILE_NOT_FOUND);

        if (!keyFile.isFile())
            throw new Exception(NOT_A_FILE);

        boolean correctExtension = CertificateConverterUtils.endsWithValidExtension(keyFile.getName());

        if (!correctExtension)
            throw new Exception(NO_VALID_FILE_EXTENSION);

        return CertificateConverterUtils.generateX509Certificate(keyFile);

    }



}
