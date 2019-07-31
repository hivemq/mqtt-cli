package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class UnsignedIntConverter implements CommandLine.ITypeConverter<Long> {
    private final static long MAX_VALUE = 4_294_967_295L;
    public static final String WRONG_INPUT_MESSAGE = "Value must be in range [0 - 4_294_967_295]";

    @Override
    public Long convert(final @NotNull String s) throws Exception {
        try {
            final Long interval = Long.parseLong(s);
            if (!(interval >= 0 && interval <= MAX_VALUE)) {
                throw new Exception(WRONG_INPUT_MESSAGE);
            }
            return interval;
        } catch (final NumberFormatException p ) {
            throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }
}
