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

package com.hivemq.cli.commands.hivemq.datahub;

import com.hivemq.cli.converters.ByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public class SchemaDefinitionOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--file"},
                        description = "The file containing the schema definition. " +
                                "This option is mutually exclusive with --definition.")
    private @Nullable String file;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--definition"},
                        converter = ByteBufferConverter.class,
                        description = "The schema definition provided directly. " +
                                "This option is mutually exclusive with --file.")
    private @Nullable ByteBuffer argument;

    public @Nullable String getFile() {
        return file;
    }

    public @Nullable ByteBuffer getArgument() {
        return argument;
    }

    @Override
    public @NotNull String toString() {
        return "SchemaDefinitionOptions{" + "file=" + file + ", definition=" + argument + '}';
    }
}
