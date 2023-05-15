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

package com.hivemq.cli.commands.hivemq.datagovernance;

import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.FileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public class SchemaDefinitionOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--file"},
                        converter = FileToByteBufferConverter.class,
                        description = "the file containing the schema definition.")
    private void setDefinitionFromFile(final @NotNull ByteBuffer definitionFromFile) {
        definition = definitionFromFile;
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--definition"},
                        converter = ByteBufferConverter.class,
                        description = "the schema definition provided directly. ")
    private void setDefinitionFromCommandline(final @NotNull ByteBuffer definitionFromArgument) {
        definition = definitionFromArgument;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private ByteBuffer definition;

    public @NotNull ByteBuffer getDefinition() {
        return definition;
    }

    @Override
    public @NotNull String toString() {
        return "SchemaDefinitionOptions{" + "definition=" + definition + '}';
    }
}
