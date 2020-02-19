package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;

public class Tuple<A, B> {
    private final A key;
    private final B value;

    public Tuple(final @NotNull A key, final @NotNull B value) {
        this.key = key;
        this.value = value;
    }

    public @NotNull A getKey() { return key; }

    public @NotNull B getValue() { return value; }

    @Override
    public String toString() {
        return "Tuple{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
