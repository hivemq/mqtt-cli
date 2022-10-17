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
package com.hivemq.cli.utils.cli;

import com.hivemq.cli.utils.OrphanProcessCleanup;
import com.hivemq.cli.utils.cli.io.ProcessIO;
import com.hivemq.cli.utils.cli.results.ExecutionResultAsync;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hivemq.cli.utils.cli.MqttCli.CLI_EXEC;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MqttCliAsync implements AfterEachCallback {

    final List<Process> startedProcesses = new ArrayList<>();

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        for (final Process startedProcess : startedProcesses) {
            startedProcess.destroyForcibly();
        }
    }

    /**
     * Executes a mqtt-cli command asynchronously. This method should be used for all mqtt-cli commands which do not
     * exit the process like the subscribe command.
     * @param command the command to execute with the mqtt cli
     * @param environmentVariables the environment variables to start the process with
     * @return an {@link ExecutionResultAsync} which can be used to wait for std-out std-err messages and write messages
     * @throws IOException when an error occurred while starting the process
     */
    public @NotNull ExecutionResultAsync executeAsync(final @NotNull List<String> command, final @NotNull Map<String, String> environmentVariables)
            throws IOException {
        final List<String> fullCommand = new ArrayList<>(CLI_EXEC);
        assertTrue(fullCommand.addAll(command));

        final ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.environment().putAll(environmentVariables);
        final Process process = processBuilder.start();

        final ProcessIO processIO = ProcessIO.startReading(process);
        final Process cliProcess = OrphanProcessCleanup.startOrphanCleanupProcess(process);

        startedProcesses.add(process);
        startedProcesses.add(cliProcess);

        return new ExecutionResultAsync(processIO, String.join(" ", fullCommand));
    }

    /**
     * Executes a mqtt-cli command asynchronously. This method should be used for all mqtt-cli commands which do not
     * exit the process like the subscribe command.
     * @param command the command to execute with the mqtt cli
     * @return an {@link ExecutionResultAsync} which can be used to wait for std-out std-err messages and write messages
     * @throws IOException when an error occurred while starting the process
     */
    public @NotNull ExecutionResultAsync executeAsync(final @NotNull List<String> command)
            throws IOException {
        return executeAsync(command, Map.of());
    }

}
