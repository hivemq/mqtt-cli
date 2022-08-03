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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CLIShellTestExtension implements BeforeEachCallback, AfterEachCallback {

    private static final @NotNull String mqttExec = "build/native/nativeCompile/mqtt-cli";

    private @Nullable Process cliShell;
    private final @NotNull CommandConsumer commandConsumer = new CommandConsumer();
    private final @NotNull CommandConsumer errorConsumer = new CommandConsumer();

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {
        cliShell = new ProcessBuilder(mqttExec, "sh").start();
        waitForStartup(cliShell).get(3, TimeUnit.SECONDS);
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
        try {
            executeCommand(command, expectedReturn).get(3, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> executeCommand(
            final @NotNull String command, final @NotNull String expectedReturn) {
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

    public void executeCommandWithErrorWithTimeout(
            final @NotNull String command, final @NotNull String expectedReturn) {
        try {
            executeCommandWithError(command, expectedReturn).get(3, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull CompletableFuture<Void> executeCommandWithError(
            final @NotNull String command, final @NotNull String expectedError) {
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

        final CompletableFuture<Void> errorReturned = errorConsumer.waitFor(expectedError);
        final StringBuilder errorBuilder = new StringBuilder();

        return CompletableFuture.runAsync(() -> {
            while (!errorReturned.isDone()) {
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
