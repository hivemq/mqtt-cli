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

import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hivemq.cli.utils.MqttCli.CLI_EXEC;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MqttCliShell implements BeforeEachCallback, AfterEachCallback {

    private @Nullable ProcessIO processIO;
    private @Nullable Process process;

    @Override
    public void beforeEach(final @NotNull ExtensionContext context) throws Exception {
        startShellMode();
    }

    @Override
    public void afterEach(final @NotNull ExtensionContext context) {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    private void startShellMode() throws IOException {
        final List<String> shellCommand = new ArrayList<>(CLI_EXEC);
        assertTrue(shellCommand.add("sh"));

        this.process = new ProcessBuilder(shellCommand).start();
        this.processIO = ProcessIO.startReading(process);

        new AwaitOutput(processIO, String.join(" ", shellCommand)).awaitStdOut("mqtt>");
    }

    public void connectClient(final @NotNull HiveMQTestContainerExtension hivemq) throws Exception {
        final List<String> connectCommand =
                List.of("con", "-h", hivemq.getHost(), "-p", String.valueOf(hivemq.getMqttPort()), "-i", "cliTest");

        executeAsync(connectCommand).awaitStdOut(String.format("cliTest@%s>", hivemq.getHost()));
    }

    public @NotNull AwaitOutput executeAsync(final @NotNull List<String> command) throws IOException {
        final String fullCommand = String.join(" ", command);

        Objects.requireNonNull(processIO).writeMsg(fullCommand);
        return new AwaitOutput(processIO, fullCommand);
    }
}