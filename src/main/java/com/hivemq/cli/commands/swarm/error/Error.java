package com.hivemq.cli.commands.swarm.error;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yannick Weber
 */
public class Error {


    private final @Nullable String detail;

    public Error(final @Nullable String detail) {
        this.detail = detail;
    }

    public @Nullable String getDetail() {
        return detail;
    }
}
