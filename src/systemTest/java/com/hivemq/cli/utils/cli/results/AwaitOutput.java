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

package com.hivemq.cli.utils.cli.results;

import com.hivemq.cli.utils.cli.io.LogWaiter;
import com.hivemq.cli.utils.cli.io.ProcessIO;
import com.hivemq.cli.utils.exceptions.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AwaitOutput {

    private final @NotNull ProcessIO processIO;
    private final @NotNull String command;
    private final @Nullable LogWaiter logWaiter;

    public AwaitOutput(
            final @NotNull ProcessIO processIO, final @Nullable LogWaiter logWaiter, final @NotNull String command) {
        this.processIO = processIO;
        this.command = command;
        this.logWaiter = logWaiter;
    }

    public @NotNull AwaitOutput awaitStdOut(final @NotNull String expectedOutput) {
        try {
            processIO.awaitStdOut(expectedOutput);
        } catch (final TimeoutException timeoutException) {
            Assertions.fail(String.format(
                    "Command '%s' did not return expected standard output '%s' in time. Actual read standard output: '%s'",
                    command,
                    expectedOutput,
                    timeoutException.getActualOutput()), timeoutException);
        }
        return this;
    }

    public @NotNull AwaitOutput awaitStdErr(final @NotNull String expectedOutput) {
        try {
            processIO.awaitStdErr(expectedOutput);
        } catch (final TimeoutException timeoutException) {
            Assertions.fail(String.format(
                    "Command '%s' did not return expected error output '%s' in time. Actual read error output: '%s'",
                    command,
                    expectedOutput,
                    timeoutException.getActualOutput()), timeoutException);
        }
        return this;
    }

    public @NotNull AwaitOutput awaitLog(final @NotNull String expectedLogMessage) {
        assertNotNull(logWaiter);
        try {
            logWaiter.awaitLog(expectedLogMessage);
        } catch (final TimeoutException timeoutException) {
            Assertions.fail(String.format(
                    "Command '%s' did not return expected logfile output '%s' in time. Actual read logfile output: '%s'",
                    command,
                    expectedLogMessage,
                    timeoutException.getActualOutput()), timeoutException);
        }
        return this;
    }
}
