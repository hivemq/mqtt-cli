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

import com.hivemq.cli.utils.MqttVersionConverter;
import com.hivemq.cli.utils.OrphanProcessCleanup;
import com.hivemq.cli.utils.broker.HiveMQExtension;
import com.hivemq.cli.utils.cli.io.LogWaiter;
import com.hivemq.cli.utils.cli.io.ProcessIO;
import com.hivemq.cli.utils.cli.results.AwaitOutput;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hivemq.cli.utils.broker.assertions.ConnectAssertion.assertConnectPacket;
import static com.hivemq.cli.utils.cli.MqttCli.CLI_EXEC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MqttCliShellExtension implements BeforeEachCallback, AfterEachCallback {

    public static @NotNull String DEFAULT_CLIENT_NAME = "cliTest";

    private @Nullable Path homeDir;
    private @Nullable ProcessIO processIO;
    private @Nullable Process process;
    private @Nullable LogWaiter logWaiter;
    private @Nullable Process orphanCleanupProcess;
    private int connectClientMarker = 0;

    private final @NotNull Map<String, String> envVariables;

    public MqttCliShellExtension() {
        envVariables = Map.of();
    }

    public MqttCliShellExtension(final @NotNull Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {
        // Setup the mqtt-cli home folder for logging, etc.
        this.homeDir = Files.createTempDirectory("mqtt-cli-home");

        // Start and await the start of the shell
        this.process = startShellMode(homeDir);
        this.orphanCleanupProcess = OrphanProcessCleanup.startOrphanCleanupProcess(process);
        this.processIO = ProcessIO.startReading(process);
        new AwaitOutput(processIO, null, String.join(" ", getShellCommand(homeDir))).awaitStdOut("mqtt>");

        // We can only initialize the logger after starting up the shell because the startup initializes the logfile
        this.logWaiter = setupLogWaiter(homeDir);
        this.connectClientMarker = 0;
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) throws IOException {
        if (homeDir != null && homeDir.toFile().exists()) {
            FileUtils.deleteDirectory(homeDir.toFile());
        }
        if (process != null) {
            process.destroyForcibly();
        }
        if (orphanCleanupProcess != null) {
            orphanCleanupProcess.destroyForcibly();
        }
    }

    private @NotNull Process startShellMode(final @NotNull Path homeDir) throws IOException {
        final List<String> shellCommand = getShellCommand(homeDir);
        final ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);
        processBuilder.environment().putAll(envVariables);
        return processBuilder.start();
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
        final File logFolder = homeDir.resolve(".mqtt-cli").resolve("logs").toFile();
        final File[] logFiles = logFolder.listFiles();
        assertNotNull(logFiles);
        assertEquals(1, logFiles.length);
        return new LogWaiter(logFiles[0]);
    }

    /**
     * Connects a mqtt-client with client-id {@link #DEFAULT_CLIENT_NAME} and awaits the successful output statements on
     * std-out and in the logfile.
     *
     * @param hivemq the HiveMQ instance to which the client should connect
     * @throws IOException when the cli command to connect could not be written to the shell
     */
    public void connectClient(final @NotNull HiveMQExtension hivemq, final char mqttVersion) throws IOException {
        connectClient(hivemq, mqttVersion, DEFAULT_CLIENT_NAME);
    }

    /**
     * Connects a mqtt-client with the given client-id and awaits the successful output statements on std-out and in the
     * logfile.
     *
     * @param hivemq the HiveMQ instance to which the client should connect
     * @throws IOException when the cli command to connect could not be written to the shell
     */
    public void connectClient(
            final @NotNull HiveMQExtension hivemq, final char mqttVersion, final @NotNull String clientId)
            throws IOException {
        final List<String> connectCommand = List.of("con",
                "-h",
                hivemq.getHost(),
                "-p",
                String.valueOf(hivemq.getMqttPort()),
                "-V",
                String.valueOf(mqttVersion),
                "-i",
                clientId);

        executeAsync(connectCommand).awaitStdOut(String.format("%s@%s>", clientId, hivemq.getHost()))
                .awaitLog("sending CONNECT")
                .awaitLog("MqttConnect")
                .awaitLog("received CONNACK")
                .awaitLog("MqttConnAck");

        assertConnectPacket(hivemq.getConnectPackets().get(connectClientMarker), connectAssertion -> {
            connectAssertion.setMqttVersion(MqttVersionConverter.toExtensionSdkVersion(mqttVersion));
            connectAssertion.setClientId(clientId);
        });

        connectClientMarker += 1;
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
        Objects.requireNonNull(processIO).writeMsg(fullCommand);
        return new AwaitOutput(processIO, logWaiter, fullCommand);
    }

    /**
     * Check if the mqtt-cli shell process is alive.
     *
     * @return true if the mqtt-cli shell process is alive.
     */
    public boolean isAlive() {
        return Objects.requireNonNull(process).isAlive();
    }

}
