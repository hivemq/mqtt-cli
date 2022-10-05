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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MqttCli {

    // See 'systemTest' and 'systemTestNative' in build.gradle.kts
    // Depending on the task used, the property cliExec either contains the absolute path to the jar or the native image executable
    public static final @NotNull List<String> CLI_EXEC =
            Arrays.stream(Objects.requireNonNull(System.getProperty("cliExec")).split(" "))
                    .collect(Collectors.toUnmodifiableList());

    /**
     * Executes a mqtt-cli command in blocking manner. This method should be used for all mqtt-cli commands which
     * exit the cli with an exit code.
     * @param command the command to execute with the mqtt cli
     * @return an {@link ExecutionResult} which contains the std-output, err-ouput and exit-code of the command's execution
     * @throws IOException when an error occurred while starting the process or reading its output
     * @throws InterruptedException when the process was interrupted
     */
    public @NotNull ExecutionResult execute(final @NotNull List<String> command) throws IOException, InterruptedException {
        final List<String> fullCommand = new ArrayList<>(CLI_EXEC);
        assertTrue(fullCommand.addAll(command));

        final Process process = new ProcessBuilder(fullCommand).start();

        final int exitCode = process.waitFor();

        final String stdOut = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final String stdErr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!stdOut.isEmpty()) {
            System.out.println(stdOut);
        }
        if (!stdErr.isEmpty()) {
            System.err.println(stdErr);
        }

        return new ExecutionResult(exitCode, stdOut, stdErr);
    }

    /**
     * Executes a mqtt-cli command asynchronously. This method should be used for all mqtt-cli commands which do not
     * exit the process like the subscribe command.
     * @param command the command to execute with the mqtt cli
     * @return an {@link AwaitOutput} which can be used to wait for std-out std-err messages
     * @throws IOException when an error occurred while starting the process
     */
    public @NotNull AwaitOutput executeAsync(final @NotNull List<String> command)
            throws IOException {
        final List<String> fullCommand = new ArrayList<>(CLI_EXEC);
        assertTrue(fullCommand.addAll(command));

        final Process process = new ProcessBuilder(fullCommand).start();
        final ProcessIO processIO = ProcessIO.startReading(process);

        return new AwaitOutput(processIO, null, String.join(" ", command));
    }

}
