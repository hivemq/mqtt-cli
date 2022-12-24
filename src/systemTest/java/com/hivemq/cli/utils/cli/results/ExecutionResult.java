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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutionResult {

    private final int exitCode;
    private final @NotNull String standardOutput;
    private final @NotNull String errorOutput;
    private final @NotNull Path homeDir;

    public ExecutionResult(
            final int exitCode,
            final @NotNull String standardOutput,
            final @NotNull String errorOutput,
            final @NotNull Path homeDir) {
        this.exitCode = exitCode;
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
        this.homeDir = homeDir;
    }

    public int getExitCode() {
        return exitCode;
    }

    public @NotNull String getStandardOutput() {
        return standardOutput;
    }

    public @NotNull String getErrorOutput() {
        return errorOutput;
    }

    private @Nullable String getLog() throws IOException {
        final File logFolder = homeDir.resolve(".mqtt-cli/logs").toFile();
        final File[] logFiles = logFolder.listFiles();
        if (logFiles == null) {
            return null;
        }
        assertEquals(1, logFiles.length);
        return Files.readString(logFiles[0].toPath());
    }
}
