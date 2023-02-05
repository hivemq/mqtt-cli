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

package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.options.DisconnectOptions;
import com.hivemq.cli.mqtt.clients.ShellClients;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "exit", description = "Exit the shell", mixinStandardHelpOptions = true)
public class ShellExitCommand implements Callable<Integer> {

    private final @NotNull ShellClients shellClients;

    @Inject
    public ShellExitCommand(final @NotNull ShellClients shellClients) {
        this.shellClients = shellClients;
    }

    @Override
    public @NotNull Integer call() {
        shellClients.disconnectAllClients(new DisconnectOptions());
        ShellCommand.exitShell();
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ShellExitCommand{}";
    }
}

