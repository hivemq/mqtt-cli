package com.hivemq.cli.converters;

import com.hivemq.cli.utils.PasswordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.inject.Inject;
import java.nio.ByteBuffer;

public class PasswordConverter implements CommandLine.ITypeConverter<ByteBuffer> {

    private final ByteBufferConverter byteBufferConverter;

    @Inject
    public PasswordConverter(final @NotNull ByteBufferConverter byteBufferConverter) {
        this.byteBufferConverter = byteBufferConverter;
    }

    @Override
    public ByteBuffer convert(@Nullable String s) throws Exception {
        if (s.isEmpty() || s.charAt(0) == '$') {
            s = new String(PasswordUtils.readPassword("Please enter the authentication password:"));
        }
        return byteBufferConverter.convert(s);
    }
}
