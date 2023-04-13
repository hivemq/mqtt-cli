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

import com.hivemq.cli.utils.cli.results.ExecutionResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MqttCli {

    // See 'systemTest' and 'systemTestNative' in build.gradle.kts
    // Depending on the task used, the property cliExec either contains the absolute path to the jar or the native image executable
    public static final @NotNull List<String> CLI_EXEC =
            Arrays.stream(Objects.requireNonNull(System.getProperty("cliExec")).split(" "))
                    .collect(Collectors.toUnmodifiableList());

    /**
     * Executes a mqtt-cli command in blocking manner. This method should be used for all mqtt-cli commands which exit
     * the cli with an exit code.
     *
     * @param command              the command to execute with the mqtt cli
     * @param environmentVariables the environment variables to start the process with
     * @param configProperties     the config properties saved inside the config.properties of the cli home folder
     * @return an {@link ExecutionResult} which contains the std-output, err-output and exit-code of the command's
     *         execution
     * @throws IOException          when an error occurred while starting the process or reading its output
     * @throws InterruptedException when the process was interrupted
     */
    public static @NotNull ExecutionResult execute(
            final @NotNull List<String> command,
            final @NotNull Map<String, String> environmentVariables,
            final @NotNull Map<String, String> configProperties) throws IOException, InterruptedException {
        final Path homeDir = Files.createTempDirectory("mqtt-cli-home");
        final Path cliConfigFolder = Files.createDirectory(homeDir.resolve(".mqtt-cli"));
        final Path propertiesFilePath = cliConfigFolder.resolve("config.properties");

        final Properties properties = new Properties();
        properties.putAll(configProperties);
        try (final OutputStream output = Files.newOutputStream(propertiesFilePath)) {
            properties.store(output, null);
        }

        final List<String> fullCommand = getCliCommand(homeDir);
        assertTrue(fullCommand.addAll(command));

        final ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.environment().putAll(environmentVariables);
        final Process process = processBuilder.start();

        final int exitCode = process.waitFor();

        final String stdOut = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final String stdErr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!stdOut.isEmpty()) {
            System.out.println(stdOut);
        }
        if (!stdErr.isEmpty()) {
            System.err.println(stdErr);
        }

        return new ExecutionResult(exitCode, stdOut, stdErr, homeDir);
    }

    /**
     * Executes a mqtt-cli command in blocking manner. This method should be used for all mqtt-cli commands which exit
     * the cli with an exit code.
     *
     * @param command              the command to execute with the mqtt cli
     * @param environmentVariables the environment variables to start the process with
     * @return an {@link ExecutionResult} which contains the std-output, err-output and exit-code of the command's
     *         execution
     * @throws IOException          when an error occurred while starting the process or reading its output
     * @throws InterruptedException when the process was interrupted
     */
    public static @NotNull ExecutionResult execute(
            final @NotNull List<String> command, final @NotNull Map<String, String> environmentVariables)
            throws IOException, InterruptedException {
        return execute(command, environmentVariables, Map.of());
    }

    /**
     * Executes a mqtt-cli command in blocking manner. This method should be used for all mqtt-cli commands which exit
     * the cli with an exit code.
     *
     * @param command the command to execute with the mqtt cli
     * @return an {@link ExecutionResult} which contains the std-output, err-ouput and exit-code of the command's
     *         execution
     * @throws IOException          when an error occurred while starting the process or reading its output
     * @throws InterruptedException when the process was interrupted
     */
    public static @NotNull ExecutionResult execute(final @NotNull List<String> command)
            throws IOException, InterruptedException {
        return execute(command, Map.of());
    }

    static @NotNull List<String> getCliCommand(final Path homeDir) {
        // Set system property 'user.home' to the temp home directory, so that the cli tests does not use the default home folder
        final ArrayList<String> shellCommand = new ArrayList<>(CLI_EXEC);
        final String homeSystemProperty = String.format("-Duser.home=%s", homeDir.toAbsolutePath());
        if (shellCommand.contains("-jar")) {
            // normal java -jar execution
            final int index = shellCommand.indexOf("-jar");
            shellCommand.add(index + 1, homeSystemProperty);
        } else {
            // Graal execution
            shellCommand.add(homeSystemProperty);
        }
        return shellCommand;
    }
}
