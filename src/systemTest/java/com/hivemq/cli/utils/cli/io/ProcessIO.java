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

package com.hivemq.cli.utils.cli.io;

import com.hivemq.cli.utils.exceptions.TimeoutException;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;

public class ProcessIO {

    private final @NotNull BufferedWriter stdoutWriter;
    private final @NotNull StringBuilder processStdOut;
    private final @NotNull StringBuilder processStdErr;
    private final @NotNull BufferedWriter processOutputWriter;
    private final @NotNull AtomicInteger processStdOutMarker;
    private final @NotNull AtomicInteger processStdErrMarker;

    private ProcessIO(
            final @NotNull BufferedWriter stdoutWriter,
            final @NotNull StringBuilder processStdOut,
            final @NotNull StringBuilder processStdErr,
            final @NotNull BufferedWriter processOutputWriter) {
        this.stdoutWriter = stdoutWriter;
        this.processStdOut = processStdOut;
        this.processStdErr = processStdErr;
        this.processOutputWriter = processOutputWriter;
        this.processStdOutMarker = new AtomicInteger(0);
        this.processStdErrMarker = new AtomicInteger(0);
    }

    public static @NotNull ProcessIO startReading(final @NotNull Process process) {
        final StringBuilder processStdOut = new StringBuilder();
        final StringBuilder processErrOut = new StringBuilder();
        final BufferedWriter processOutputWriter =
                new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        /* Startup std-out writer */
        final InputStream processInputStream = process.getInputStream();
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));

        CompletableFuture.runAsync(() -> {
            try {
                int previousChar = '\0';
                int readChar;
                while ((readChar = processInputStream.read()) != -1) {
                    writer.write(readChar);
                    processStdOut.append((char) readChar);

                    if ((previousChar == '>' && readChar == ' ') || readChar == '\n') {
                        writer.flush();
                    }

                    previousChar = readChar;
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });

        /* Startup std-err writer */
        final InputStream processErrorStream = process.getErrorStream();
        final BufferedWriter errorWriter =
                new BufferedWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8));

        CompletableFuture.runAsync(() -> {
            try {
                int readChar;
                while ((readChar = processErrorStream.read()) != -1) {
                    errorWriter.write(readChar);
                    processErrOut.append((char) readChar);

                    if (readChar == '\n') {
                        errorWriter.flush();
                    }
                }
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });

        return new ProcessIO(writer, processStdOut, processErrOut, processOutputWriter);
    }

    public void awaitStdOut(final @NotNull String stdOutMessage) throws TimeoutException {
        try {
            await().until(() -> {
                final int index = processStdOut.indexOf(stdOutMessage, processStdOutMarker.get());

                if (index == -1) {
                    return false;
                }

                processStdOutMarker.set(index + stdOutMessage.length());
                return true;
            });
        } catch (final @NotNull ConditionTimeoutException timeoutException) {
            throw new TimeoutException(timeoutException, processStdOut.substring(processStdOutMarker.get()));
        }
    }

    public void awaitStdErr(final @NotNull String stdErrMessage) throws TimeoutException {
        try {
            await().until(() -> {
                final int index = processStdErr.indexOf(stdErrMessage, processStdErrMarker.get());

                if (index == -1) {
                    return false;
                }

                processStdErrMarker.set(index + stdErrMessage.length());
                return true;
            });
        } catch (final ConditionTimeoutException timeoutException) {
            throw new TimeoutException(timeoutException, processStdErr.substring(processStdErrMarker.get()));
        }

    }

    public void writeMsg(final @NotNull String message) throws IOException {
        // We write these messages to stdout so that the console output of the tests make sense
        stdoutWriter.write(message);
        stdoutWriter.write('\n');
        stdoutWriter.flush();

        // This is the actual writing to the process which gets interpreted
        processOutputWriter.write(message);
        processOutputWriter.write('\n');
        processOutputWriter.flush();
    }
}
