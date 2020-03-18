package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class PasswordFileToByteBufferConverterTest {

    PasswordFileToByteBufferConverter passwordFileToByteBufferConverter;

    @BeforeEach
    void setUp() {
        passwordFileToByteBufferConverter = new PasswordFileToByteBufferConverter();
    }

    @Test
    void test_single_Line() throws Exception {
        final File file = getFile("password_with_single_line.txt");

        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();

        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }

    @Test
    void test_single_newLine() throws Exception {
        final File file = getFile("password_with_new_line.txt");

        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();

        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }

    @Test
    void test_multi_newLine() throws Exception {
        final File file = getFile("password_with_multi_new_line.txt");

        final byte[] expected = "Z$a8o7PQ3wnoA%=F%Bx*cevXRym44y+NRFWiEA3C".getBytes();

        final byte[] actual = passwordFileToByteBufferConverter.convert(file.getPath()).array();

        assertArrayEquals(expected, actual);
    }


    private File getFile(final @NotNull String fileName) {
        return new File(getClass().getClassLoader().getResource(this.getClass().getSimpleName() + "/" + fileName).getPath());
    }
}