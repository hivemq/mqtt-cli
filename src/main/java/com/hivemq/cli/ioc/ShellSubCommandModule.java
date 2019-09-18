/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.shell.ShellDisconnectCommand;
import com.hivemq.cli.commands.shell.*;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ShellSubCommandModule {

    @Singleton
    @Provides
    @Named("shell-sub-command")
    static @NotNull CommandLine provideShellCommand(final @NotNull ShellCommand shellCommand,
                                                    final @NotNull VersionCommand versionCommand,
                                                    final @NotNull ShellConnectCommand shellConnectCommand,
                                                    final @NotNull ShellDisconnectCommand disconnectCommand,
                                                    final @NotNull ContextSwitchCommand contextSwitchCommand,
                                                    final @NotNull ClearScreenCommand clearScreenCommand,
                                                    final @NotNull ListClientsCommand listClientsCommand,
                                                    final @NotNull ShellExitCommand shellExitCommand) {

        return new CommandLine(shellCommand)
                .addSubcommand(CommandLine.HelpCommand.class)
                .addSubcommand(versionCommand)
                .addSubcommand(shellConnectCommand)
                .addSubcommand(disconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(shellExitCommand);
    }
}
