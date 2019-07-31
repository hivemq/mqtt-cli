package com.hivemq.cli.converters;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class UnsignedShortConverter implements CommandLine.ITypeConverter<Integer> {

    private final static int MAX_VALUE = 65_535;
    public static final String WRONG_INPUT_MESSAGE = "Value must be in range [0 - 65_535]";

    @Override
    public Integer convert(final @NotNull String s) throws Exception {
        try {
            final Integer interval = Integer.parseInt(s);
            if (!(interval >= 0 && interval <= MAX_VALUE)) {
                throw new Exception(WRONG_INPUT_MESSAGE);
            }
            return interval;
        } catch (final NumberFormatException p) {
            throw new Exception(WRONG_INPUT_MESSAGE);
        }
    }

}
