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
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CLIShellTestExtension implements BeforeEachCallback, AfterEachCallback {

    public static final @NotNull List<String> CLI_EXEC =
            Arrays.stream(Objects.requireNonNull(System.getProperty("cliExec")).split(" "))
                    .collect(Collectors.toList());

    private static final int TIMEOUT = 10;

    private @Nullable Process cliShell;
    private final @NotNull CommandConsumer commandConsumer = new CommandConsumer();
    private final @NotNull CommandConsumer errorConsumer = new CommandConsumer();

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {
        final ArrayList<String> shellCommand = new ArrayList<>(CLI_EXEC);
        shellCommand.add("sh");
        cliShell = new ProcessBuilder(shellCommand).start();
        waitForStartup(cliShell).get(TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        if (cliShell == null) {
            throw new IllegalStateException();
        }
        cliShell.destroy();
    }

    private @NotNull CompletableFuture<Void> waitForStartup(final @NotNull Process cliShell) {
        final InputStream inputStream = cliShell.getInputStream();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedInputReader = new BufferedReader(inputStreamReader);

        final CompletableFuture<Void> cliReady = commandConsumer.waitFor("mqtt>");
        final StringBuilder commandBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (!cliReady.isDone()) {
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

    public void executeCommandWithTimeout(final @NotNull String command, final @NotNull String expectedReturn) {
        executeCommandWithTimeout(command, Set.of(expectedReturn));
    }

    public @NotNull CompletableFuture<Void> executeCommand(
            final @NotNull String command, final @NotNull String expectedReturn) {
        return executeCommand(command, Set.of(expectedReturn));
    }

    public void executeCommandWithTimeout(final @NotNull String command, final @NotNull Set<String> expectedReturns) {
        try {
            executeCommand(command, expectedReturns).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> executeCommand(
            final @NotNull String command, final @NotNull Set<String> expectedReturns) {
        if (cliShell == null) {
            throw new IllegalStateException();
        }

        try {
            final OutputStream outputStream = cliShell.getOutputStream();
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            final BufferedWriter bufferedOutputWriter = new BufferedWriter(outputStreamWriter);
            bufferedOutputWriter.write(command);
            bufferedOutputWriter.write("\n");
            bufferedOutputWriter.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final InputStream inputStream = cliShell.getInputStream();
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedInputReader = new BufferedReader(inputStreamReader);

        final ArrayList<CompletableFuture<Void>> commandFinished = new ArrayList<>();
        for (final String expectedReturn : expectedReturns) {
            commandFinished.add(commandConsumer.waitFor(expectedReturn));
        }
        final StringBuilder commandBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (commandFinished.stream().filter(CompletableFuture::isDone).findAny().isEmpty()) {
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

    public void executeCommandWithErrorWithTimeout(
            final @NotNull String command, final @NotNull String expectedError) {
        executeCommandWithErrorWithTimeout(command, Set.of(expectedError));
    }

    public @NotNull CompletableFuture<Void> executeCommandWithError(
            final @NotNull String command, final @NotNull String expectedError) {
        return executeCommandWithError(command, Set.of(expectedError));
    }

    public void executeCommandWithErrorWithTimeout(
            final @NotNull String command, final @NotNull Set<String> expectedErrors) {
        try {
            executeCommandWithError(command, expectedErrors).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> executeCommandWithError(
            final @NotNull String command, final @NotNull Set<String> expectedErrors) {
        if (cliShell == null) {
            throw new IllegalStateException();
        }

        try {
            final OutputStream outputStream = cliShell.getOutputStream();
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            final BufferedWriter bufferedOutputWriter = new BufferedWriter(outputStreamWriter);
            bufferedOutputWriter.write(command);
            bufferedOutputWriter.write("\n");
            bufferedOutputWriter.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final InputStream errorStream = cliShell.getErrorStream();
        final InputStreamReader errorStreamReader = new InputStreamReader(errorStream, StandardCharsets.UTF_8);
        final BufferedReader bufferedErrorReader = new BufferedReader(errorStreamReader);

        final ArrayList<CompletableFuture<Void>> commandFinished = new ArrayList<>();
        for (final String expectedError : expectedErrors) {
            commandFinished.add(errorConsumer.waitFor(expectedError));
        }
        final StringBuilder errorBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (commandFinished.stream().filter(CompletableFuture::isDone).findAny().isEmpty()) {
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
