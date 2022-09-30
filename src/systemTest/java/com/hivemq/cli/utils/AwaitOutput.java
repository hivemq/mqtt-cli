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
package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AwaitOutput {

    private final @NotNull ProcessIO processIO;
    private final @NotNull String command;

    public AwaitOutput(final @NotNull ProcessIO processIO, final @NotNull String command) {
        this.processIO = processIO;
        this.command = command;
    }

    public @NotNull AwaitOutput awaitStdout(final @NotNull String expectedOutput) {
        final StringBuilder outputUntilStop = new StringBuilder();
        final CompletableFuture<Boolean> awaitMsg = processIO.awaitStdOutMessage(expectedOutput, outputUntilStop);
        try {
            assertEquals(true, awaitMsg.get(10, TimeUnit.SECONDS),
                    String.format("Expected command '%s' to produce output '%s' but only read '%s'", command, expectedOutput, outputUntilStop));
        } catch (final InterruptedException | ExecutionException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        } catch (final TimeoutException timeoutException) {
            Assertions.fail(String.format("Timeout: Expected command '%s' to produce output '%s' but only read '%s'", command, expectedOutput, outputUntilStop));
        }
        return this;
    }

    public @NotNull AwaitOutput awaitStdErr(final @NotNull String expectedOutput) {
        final StringBuilder outputUntilStop = new StringBuilder();
        final CompletableFuture<Boolean> awaitMsg = processIO.awaitStdErrMessage(expectedOutput, outputUntilStop);
        try {
            assertEquals(true, awaitMsg.get(10, TimeUnit.SECONDS),
                    String.format("Expected command '%s' to produce error output '%s' but only read '%s'", command, expectedOutput, outputUntilStop));
        } catch (final InterruptedException | ExecutionException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        } catch (final TimeoutException timeoutException) {
            Assertions.fail(String.format("Timeout: Expected command '%s' to produce error output '%s' but only read '%s'", command, expectedOutput, outputUntilStop));
        }
        return this;
    }
    public @NotNull AwaitOutput awaitLog(final @NotNull String expectedLogMessage) {
        //FIXME
        return null;
    }
}
