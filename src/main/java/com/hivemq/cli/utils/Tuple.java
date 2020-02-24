package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tuple<A, B> {
    private final A key;
    private final B value;

    public Tuple(final @NotNull A key, final @Nullable B value) {
        this.key = key;
        this.value = value;
    }

    public @NotNull A getKey() { return key; }

    public @Nullable B getValue() { return value; }

    @Override
    public String toString() {
        return "Tuple{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
