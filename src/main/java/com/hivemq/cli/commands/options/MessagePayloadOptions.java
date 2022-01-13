package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.ByteBufferConverter;
import com.hivemq.cli.converters.FileToByteBufferConverter;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.nio.ByteBuffer;

public class MessagePayloadOptions {

    @CommandLine.Option(names = {"-m", "--message"}, converter = ByteBufferConverter.class, description = "The message to publish", order = 1)
    public void setMessageFromCommandline(final @NotNull ByteBuffer messageFromFile) {
        messageBuffer = messageFromFile;
    }

    @CommandLine.Option(names = {"-m:file", "--message-file"}, converter = FileToByteBufferConverter.class, description = "The message read in from a file", order = 1)
    public void setMessageFromFile(final @NotNull ByteBuffer messageFromFile) {
        messageBuffer = messageFromFile;
    }

    private @NotNull ByteBuffer messageBuffer;

    public @NotNull ByteBuffer getMessageBuffer() {
        return messageBuffer;
    }
}
