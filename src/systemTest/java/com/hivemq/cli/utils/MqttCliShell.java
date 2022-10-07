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

import com.google.common.io.Resources;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.hivemq.cli.utils.MqttCli.CLI_EXEC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MqttCliShell implements BeforeEachCallback, AfterEachCallback {

    private ProcessIO processIO;
    private Process process;
    private LogWaiter logWaiter;
    private Process orphanCleanupProcess;

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {

        // Setup the mqtt-cli home folder for logging, etc.
        final Path homeDir = Files.createTempDirectory("mqtt-cli-home");
        homeDir.toFile().deleteOnExit();

        // Start and await the start of the shell
        this.process = startShellMode(homeDir);
        this.orphanCleanupProcess = startOrphanCleanupProcess(process);
        this.processIO = ProcessIO.startReading(process);
        new AwaitOutput(processIO, null, String.join(" ", getShellCommand(homeDir))).awaitStdOut("mqtt>");

        // We can only initialize the logger after starting up the shell because the startup initializes the logfile
        this.logWaiter = setupLogWaiter(homeDir);
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        process.destroyForcibly();
        orphanCleanupProcess.destroyForcibly();
    }

    private @NotNull Process startOrphanCleanupProcess(final @NotNull Process childProcess) throws IOException {
        // We start the ProcessGarbageCollector which sole job is to destroy the childProcess, meaning the mqtt-cli shell,
        // when the jvm process exited
        final long jvmProcessId = ProcessHandle.current().pid();
        final List<String> orphanCleanupProcessCommand = List.of(
                System.getProperty("java"),
                Resources.getResource("OrphanCleanupProcess.java").getPath(),
                String.valueOf(jvmProcessId),
                String.valueOf(childProcess.pid())
        );
        final Process orphanCleanupProcess = new ProcessBuilder(orphanCleanupProcessCommand).start();

        // Wait until the process prints X, which means that the process garbage collector has registered the
        final int readChar = orphanCleanupProcess.getInputStream().read();
        assertEquals('X', readChar);

        return orphanCleanupProcess;
    }

    private @NotNull Process startShellMode(final @NotNull Path homeDir) throws IOException {
        final List<String> shellCommand = getShellCommand(homeDir);
        return new ProcessBuilder(shellCommand).start();
    }

    private @NotNull List<String> getShellCommand(final Path homeDir) {
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
        shellCommand.add("shell");
        shellCommand.add("-l");
        return shellCommand;
    }

    private @NotNull LogWaiter setupLogWaiter(final @NotNull Path homeDir) {
        final File logFolder = homeDir.resolve(".mqtt-cli/logs").toFile();
        final File[] logFiles = logFolder.listFiles();
        assertNotNull(logFiles);
        assertEquals(1, logFiles.length);
        return new LogWaiter(logFiles[0]);
    }

    /**
     * Connects a mqtt-client and awaits the successful output statements on std-out and in the logfile.
     *
     * @param hivemq the HiveMQ container to which the client should connect
     * @throws IOException when the cli command to connect could not be written to the shell
     */
    public void connectClient(final @NotNull HiveMQTestContainerExtension hivemq) throws IOException {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()), "-i", "cliTest");


        final AwaitOutput awaitOutput =
                executeAsync(connectCommand).awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
        awaitOutput.awaitLog("received CONNACK");
    }

    /**
     * Executes a mqtt-cli command asynchronously in the shell.
     *
     * @param command the command to write and execute in the shell.
     * @return an {@link AwaitOutput} which can be used to await std-output, err-output and logfile-messages of the
     *         cli.
     * @throws IOException when the cli command could not be written to the shell
     */
    public @NotNull AwaitOutput executeAsync(final @NotNull List<String> command) throws IOException {
        final String fullCommand = String.join(" ", command);
        processIO.writeMsg(fullCommand);
        return new AwaitOutput(processIO, logWaiter, fullCommand);
    }

}
