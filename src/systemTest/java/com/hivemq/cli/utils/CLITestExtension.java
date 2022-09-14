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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CLITestExtension {

    public static final @NotNull List<String> CLI_EXEC =
            Arrays.stream(Objects.requireNonNull(System.getProperty("cliExec")).split(" "))
                    .collect(Collectors.toList());

    private static final int TIMEOUT = 10;

    private final @NotNull CommandConsumer commandConsumer = new CommandConsumer();
    private final @NotNull CommandConsumer errorConsumer = new CommandConsumer();

    public void waitForOutputWithTimeout(final @NotNull Process process, final @NotNull String expectedReturn) {
        try {
            waitForOutput(process, expectedReturn).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> waitForOutput(
            final @NotNull Process process, final @NotNull String expectedReturn) {
        final InputStream inputStream = process.getInputStream();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedInputReader = new BufferedReader(inputStreamReader);

        final CompletableFuture<Void> commandReturned = commandConsumer.waitFor(expectedReturn);
        final StringBuilder commandBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (!commandReturned.isDone()) {
                try {
                    final int inputChar;
                    inputChar = bufferedInputReader.read();

                    if (inputChar == -1) {
                        throw new IllegalStateException("End of stream was reached, but command was not found.");
                    }
                    commandBuilder.append((char) inputChar);
                    commandConsumer.accept(commandBuilder.toString());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, runnable -> new Thread(runnable).start());
    }

    public void waitForErrorWithTimeout(
            final @NotNull Process process, final @NotNull String expectedError) {
        waitForErrorWithTimeout(process, Set.of(expectedError));
    }

    public void waitForErrorWithTimeout(
            final @NotNull Process process, final @NotNull Set<String> expectedErrors) {
        try {
            waitForError(process, expectedErrors).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> waitForError(
            final @NotNull Process process, final @NotNull Set<String> expectedErrors) {
        final InputStream errorStream = process.getErrorStream();
        final InputStreamReader errorStreamReader = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedErrorReader = new BufferedReader(errorStreamReader);

        final ArrayList<CompletableFuture<Void>> errorReadFutures = new ArrayList<>();
        for (final String expectedError : expectedErrors) {
            errorReadFutures.add(errorConsumer.waitFor(expectedError));
        }
        final StringBuilder errorBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (errorReadFutures.stream().filter(CompletableFuture::isDone).findAny().isEmpty()) {
                try {
                    final int inputChar = bufferedErrorReader.read();
                    if (inputChar == -1) {
                        throw new IllegalStateException("End of stream was reached, but error was not found.");
                    }
                    errorBuilder.append((char) inputChar);
                    errorConsumer.accept(errorBuilder.toString());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, runnable -> new Thread(runnable).start());
    }
}
