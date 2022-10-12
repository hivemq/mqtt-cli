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

import java.io.IOException;

public class ExecutionResultAsync {

    private final @NotNull AwaitOutput awaitOutput;
    private final @NotNull ProcessIO processIO;

    public ExecutionResultAsync(final @NotNull AwaitOutput awaitOutput, final @NotNull ProcessIO processIO) {
        this.awaitOutput = awaitOutput;
        this.processIO = processIO;
    }

    public @NotNull AwaitOutput getAwaitOutput() {
        return awaitOutput;
    }

    public void write(final @NotNull String output) throws IOException {
        processIO.writeMsg(output);
    }
}
