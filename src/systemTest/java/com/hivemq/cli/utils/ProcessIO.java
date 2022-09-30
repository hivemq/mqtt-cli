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

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ProcessIO {

    private final @NotNull BufferedReader reader;
    private final @NotNull BufferedReader readerErr;
    private final @NotNull BufferedWriter writer;
    private final @NotNull PrintStream stdout;

    private ProcessIO(
            final @NotNull BufferedReader reader,
            final @NotNull BufferedReader readerErr,
            final @NotNull BufferedWriter writer,
            final @NotNull PrintStream stdout) {
        this.reader = reader;
        this.readerErr = readerErr;
        this.writer = writer;
        this.stdout = stdout;
    }

    public static @NotNull ProcessIO startReading(final @NotNull Process process) throws IOException {

        final PipedInputStream pipeIn = new PipedInputStream();
        final PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(pipeIn, StandardCharsets.UTF_8));
        final BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        final PrintStream stdout = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        final TeeOutputStream teeOut = new TeeOutputStream(pipeOut, stdout);
        final TeeInputStream teeInStdOut = new TeeInputStream(process.getInputStream(), teeOut);

        CompletableFuture.runAsync(() -> {
            try {
                int previousChar = '\0';
                while (true) {
                    final int readChar = teeInStdOut.read();

                    if (readChar == -1) {
                        return;
                    }

                    if (previousChar == '>' && readChar == ' ') {
                        stdout.flush();
                    }

                    previousChar = readChar;
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });


        final PipedInputStream pipeInErr = new PipedInputStream();
        final PipedOutputStream pipeOutErr = new PipedOutputStream(pipeInErr);
        final BufferedReader readerErr = new BufferedReader(new InputStreamReader(pipeInErr, StandardCharsets.UTF_8));
        final PrintStream stdErr = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        final TeeOutputStream teeOutErr = new TeeOutputStream(pipeOutErr, stdErr);
        final TeeInputStream teeInStdErr = new TeeInputStream(process.getErrorStream(), teeOutErr);

        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    final int readChar = teeInStdErr.read();

                    if (readChar == -1) {
                        return;
                    }

                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });

        return new ProcessIO(reader, readerErr, writer, stdout);
    }


    public @NotNull CompletableFuture<Boolean> awaitStdOutMessage(final @NotNull String message, final @NotNull StringBuilder outputUntilStop) {
        return awaitMessage(reader, message, outputUntilStop);
    }

    public @NotNull CompletableFuture<Boolean> awaitStdErrMessage(final @NotNull String message, final @NotNull StringBuilder outputUntilStop) {
        return awaitMessage(readerErr, message, outputUntilStop);
    }

    private CompletableFuture<Boolean> awaitMessage(final @NotNull BufferedReader reader, final @NotNull String message, final @NotNull StringBuilder outputUntilStop) {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    for (int i = 0; i < message.length(); i++) {

                        // Read next character from the input stream
                        final int readChar = reader.read();

                        // End of stream reached
                        if (readChar == -1) {
                            Assertions.fail("Reached end of stream.");
                            return false;
                        }

                        outputUntilStop.append((char) readChar);

                        // Start new iteration
                        if (readChar != message.charAt(i)) {
                            break;
                        }

                        // Message is contained
                        if ((i == message.length() - 1)) {
                            return true;
                        }
                    }
                } catch (final IOException ex) {
                    Assertions.fail(ex);
                    ex.printStackTrace();
                    return false;
                }
            }
        });

    }

    public void writeMsg(final @NotNull String message) throws IOException {
        stdout.write(message.getBytes(StandardCharsets.UTF_8));
        stdout.write('\n');

        writer.write(message);
        writer.write("\n");
        writer.flush();
    }

}
