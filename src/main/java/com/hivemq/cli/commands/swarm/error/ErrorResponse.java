package com.hivemq.cli.commands.swarm.error;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yannick Weber
 */
public class ErrorResponse {

    private final @NotNull List<Error> errors;

    public ErrorResponse(final @NotNull List<Error> errors) {
        this.errors = errors;
    }

    public @NotNull List<Error> getErrors() {
        return errors;
    }

}