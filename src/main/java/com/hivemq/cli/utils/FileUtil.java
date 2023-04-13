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
package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class FileUtil {

    static final @NotNull String FILE_NOT_FOUND = "The given file was not found.";
    static final @NotNull String NOT_A_FILE = "The given path does not lead to a valid file.";

    public static @NotNull File assertFileExists(final @NotNull Path value) throws Exception {
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
