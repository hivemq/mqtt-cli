/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class CertificateConverterUtils {
    public static final String[] FILE_EXTENSIONS = {".pem", ".cer", ".crt"};
    public static final String NO_VALID_CERTIFICATE = "The given file contains no valid or supported certficate,";

    public static @NotNull Collection<X509Certificate> generateX509Certificates(final @NotNull File keyFile) throws Exception {

        // Instantiate X509 certificate factory
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try {

            // Parse X509 certificate chain
            final Collection<? extends Certificate> certificateChainCollection = cf.generateCertificates(new FileInputStream(keyFile));

            // Cast to X509Certificate collection and return it
            return (Collection<X509Certificate>) certificateChainCollection;

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
