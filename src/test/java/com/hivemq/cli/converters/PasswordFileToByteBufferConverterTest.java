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

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordFileToByteBufferConverterTest {

    private @NotNull PasswordFileToByteBufferConverter passwordFileToByteBufferConverter;

    @BeforeEach
    void setUp() {
        passwordFileToByteBufferConverter = new PasswordFileToByteBufferConverter();
    }

    @Test
    void single_line() throws Exception {
        final File file = getFile("password_with_single_line.txt");
        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();
        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }

    @Test
    void single_newLine() throws Exception {
        final File file = getFile("password_with_new_line.txt");
        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();
        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }

    @Test
    void multi_newLine() throws Exception {
        final File file = getFile("password_with_multi_new_line.txt");
        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();
        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }

    private @NotNull File getFile(final @NotNull String fileName) throws URISyntaxException {
        final URL resource = getClass().getResource("/" + getClass().getSimpleName() + "/" + fileName);
        assertNotNull(resource);
        return new File(resource.toURI());
    }
}
