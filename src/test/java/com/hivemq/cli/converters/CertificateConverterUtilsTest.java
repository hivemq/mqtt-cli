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
package com.hivemq.cli.converters;

import com.hivemq.cli.utils.CertificateConverterUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificateConverterUtilsTest {
    private String pathToValidCertficate;
    private String pathToInvalidCertificate;


    @BeforeEach
    void setUp() {
        final URL validCertificateResource = getClass().getClassLoader().getResource("FileToCertificateConverter/validCertificate.pem");
        final URL invalidCertificate = getClass().getClassLoader().getResource("FileToCertificateConverter/invalidCertificate.pem");

        assert validCertificateResource != null;
        assert invalidCertificate != null;

        pathToValidCertficate = validCertificateResource.getPath();
        pathToInvalidCertificate = invalidCertificate.getPath();
    }

    @Test
    void generateX509Certificate_Success() throws Exception {
        X509Certificate cert = CertificateConverterUtils.generateX509Certificate(new File(pathToValidCertficate));
        assertNotNull(cert);
    }

    @Test
    void generateX509Certificate_Failure() {
        Exception e = assertThrows(CertificateException.class, () -> CertificateConverterUtils.generateX509Certificate(new File(pathToInvalidCertificate)));
        assertEquals(CertificateConverterUtils.NO_VALID_CERTIFICATE, e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.pem", "test.cer", "test.crt", "pem.pem", "cert.cer", "crt.crt", "pem.cert.crt", "cer.crt"})
    void endsWithValidExtension_Success(final @NotNull String fileName) {
        assertTrue(CertificateConverterUtils.endsWithValidExtension(fileName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.pe", "test.cr", "test.cert", "cer.ctr", "", " ", "pem", "crt", "cer", "pempem", "test.pem.ext"})
    void endsWithValidExtension_Failure(final @NotNull String fileName) {
        assertFalse(CertificateConverterUtils.endsWithValidExtension(fileName));
    }
}