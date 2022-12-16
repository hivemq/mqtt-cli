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

package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.FileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public class MessagePayloadOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-m", "--message"},
                        converter = ByteBufferConverter.class,
                        description = "The message to publish")
    private void setMessageFromCommandline(final @NotNull ByteBuffer messageFromFile) {
        messageBuffer = messageFromFile;
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-m:empty", "--message-empty"},
                        defaultValue = "false",
                        description = "Sets the message to an empty payload")
    private void setMessageToEmpty(final boolean isEmpty) {
        if (isEmpty) {
            messageBuffer = ByteBuffer.allocate(0);
        }
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-m:file", "--message-file"},
                        converter = FileToByteBufferConverter.class,
                        description = "The message read in from a file")
    private void setMessageFromFile(final @NotNull ByteBuffer messageFromFile) {
        messageBuffer = messageFromFile;
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private @NotNull ByteBuffer messageBuffer;

    public @NotNull ByteBuffer getMessageBuffer() {
        return messageBuffer;
    }

    @Override
    public @NotNull String toString() {
        return "MessagePayloadOptions{" + "messageBuffer=" + messageBuffer + '}';
    }
}
