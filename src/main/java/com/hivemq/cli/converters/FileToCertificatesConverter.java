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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

public class FileToCertificatesConverter implements CommandLine.ITypeConverter<Collection<? extends Certificate>> {

    static final String NO_VALID_FILE_EXTENSION = "The given file does not conform to a valid Certificate File Extension as " + Arrays.toString(CertificateConverterUtils.FILE_EXTENSIONS);

    @Override
    public @NotNull Collection<X509Certificate> convert(final @NotNull String s) throws Exception {

        FileConverter fileConverter = new FileConverter();
        final File keyFile = fileConverter.convert(s);

        final boolean correctExtension = CertificateConverterUtils.endsWithValidExtension(keyFile.getName());

        if (!correctExtension)
            throw new IllegalArgumentException(NO_VALID_FILE_EXTENSION);

        return CertificateConverterUtils.generateX509Certificates(keyFile);

    }



}
