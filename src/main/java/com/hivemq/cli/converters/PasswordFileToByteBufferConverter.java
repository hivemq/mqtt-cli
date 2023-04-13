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

import com.hivemq.cli.utils.FileUtil;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PasswordFileToByteBufferConverter implements CommandLine.ITypeConverter<ByteBuffer> {

    @Override
    public @NotNull ByteBuffer convert(final @NotNull String value) throws Exception {
        final File file = FileUtil.assertFileExists(Paths.get(value));
        try (final BufferedReader in = Files.newBufferedReader(file.toPath())) {
            return ByteBuffer.wrap(in.readLine().getBytes());
        }
    }
}
