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
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

public class DirectoryToCertificatesConverter implements CommandLine.ITypeConverter<Collection<X509Certificate>> {

    static final @NotNull String DIRECTORY_NOT_FOUND = "The given directory was not found.";
    static final @NotNull String NOT_A_DIRECTORY = "The given path is not a valid directory";
    static final @NotNull String NO_CERTIFICATES_FOUND_IN_DIRECTORY =
            "The given directory does not contain any valid certificates";

    @Override
    public @NotNull Collection<X509Certificate> convert(final @NotNull String s) throws Exception {
        final File directory = new File(s);

        if (!directory.exists()) {
            throw new FileNotFoundException(DIRECTORY_NOT_FOUND);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(NOT_A_DIRECTORY);
        }

        final File[] validFiles =
                directory.listFiles((dir, name) -> CertificateConverterUtils.endsWithValidExtension(name));

        if (validFiles == null || validFiles.length == 0) {
            throw new IllegalArgumentException(NO_CERTIFICATES_FOUND_IN_DIRECTORY);
        }

        final Collection<X509Certificate> certificates = new ArrayList<>();

        for (final File validFile : validFiles) {
            certificates.addAll(CertificateConverterUtils.generateX509Certificates(validFile));
        }

        return certificates;
    }
}
