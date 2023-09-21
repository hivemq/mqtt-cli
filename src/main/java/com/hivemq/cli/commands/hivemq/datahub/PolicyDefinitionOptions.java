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
import com.hivemq.cli.converters.FileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PolicyDefinitionOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--file"},
                        converter = FileToByteBufferConverter.class,
                        description = "The JSON file containing the policy definition. " +
                                "This option is mutually exclusive with --definition.")
    private void setDefinitionFromFile(final @NotNull ByteBuffer definitionFromFile) {
        definition = new String(definitionFromFile.array(), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--definition"},
                        converter = ByteBufferConverter.class,
                        description = "The policy definition provided directly. " +
                                "This option is mutually exclusive with --file.")
    private void setDefinitionFromCommandline(final @NotNull ByteBuffer definitionFromArgument) {
        definition = new String(definitionFromArgument.array(), StandardCharsets.UTF_8);
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    public @NotNull String definition;

    public @NotNull String getDefinition() {
        return definition;
    }

    @Override
    public @NotNull String toString() {
        return "PolicyDefinitionOptions{" + "definition=" + definition + '}';
    }
}
