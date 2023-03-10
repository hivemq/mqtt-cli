package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class FileUtil {

    static final @NotNull String FILE_NOT_FOUND = "The given file was not found.";
    static final @NotNull String NOT_A_FILE = "The given path does not lead to a valid file.";

    public static @NotNull File convert(final @NotNull Path value) throws Exception {
        final File file = new File(value.toUri());

        if (!file.exists()) {
            throw new FileNotFoundException(FILE_NOT_FOUND);
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException(NOT_A_FILE);
        }

        return file;
    }
}
