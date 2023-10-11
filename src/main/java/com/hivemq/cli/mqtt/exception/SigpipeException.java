package com.hivemq.cli.mqtt.exception;

import org.jetbrains.annotations.NotNull;

public class SigpipeException extends RuntimeException {

    public SigpipeException(final @NotNull String message) {
        super(message);
    }
}
