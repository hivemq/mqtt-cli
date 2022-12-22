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

package com.hivemq.cli.ioc;

import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commandline.ShellErrorMessageHandler;
import com.hivemq.cli.commands.shell.ClearScreenCommand;
import com.hivemq.cli.commands.shell.ContextDisconnectCommand;
import com.hivemq.cli.commands.shell.ContextExitCommand;
import com.hivemq.cli.commands.shell.ContextPublishCommand;
import com.hivemq.cli.commands.shell.ContextSubscribeCommand;
import com.hivemq.cli.commands.shell.ContextSwitchCommand;
import com.hivemq.cli.commands.shell.ContextUnsubscribeCommand;
import com.hivemq.cli.commands.shell.ListClientsCommand;
import com.hivemq.cli.commands.shell.ShellCommand;
import com.hivemq.cli.commands.shell.ShellConnectCommand;
import com.hivemq.cli.commands.shell.ShellContextCommand;
import com.hivemq.cli.commands.shell.ShellDisconnectCommand;
import com.hivemq.cli.commands.shell.ShellExitCommand;
import com.hivemq.cli.commands.shell.VersionCommand;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
class ShellModule {

    @Provides
    @Singleton
    @Named("shell")
    static @NotNull CommandLine provideShell(
            final @NotNull ShellCommand shellCommand,
            final @NotNull VersionCommand versionCommand,
            final @NotNull ShellConnectCommand shellConnectCommand,
            final @NotNull ShellDisconnectCommand disconnectCommand,
            final @NotNull ContextSwitchCommand contextSwitchCommand,
            final @NotNull ClearScreenCommand clearScreenCommand,
            final @NotNull ListClientsCommand listClientsCommand,
            final @NotNull ShellExitCommand shellExitCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull ShellErrorMessageHandler handler) {
        return new CommandLine(shellCommand) //
                .addSubcommand(CommandLine.HelpCommand.class)
                .addSubcommand(versionCommand)
                .addSubcommand(shellConnectCommand)
                .addSubcommand(disconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(shellExitCommand)
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }

    @Provides
    @Singleton
    @Named("shell-context")
    static @NotNull CommandLine provideShellContext(
            final @NotNull ShellContextCommand main,
            final @NotNull ContextPublishCommand contextPublishCommand,
            final @NotNull ContextSubscribeCommand contextSubscribeCommand,
            final @NotNull ContextUnsubscribeCommand contextUnsubscribeCommand,
            final @NotNull ShellConnectCommand shellConnectCommand,
            final @NotNull ContextDisconnectCommand contextDisconnectCommand,
            final @NotNull ContextSwitchCommand contextSwitchCommand,
            final @NotNull ContextExitCommand contextExitCommand,
            final @NotNull ListClientsCommand listClientsCommand,
            final @NotNull ClearScreenCommand clearScreenCommand,
            final @NotNull VersionCommand versionCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull ShellErrorMessageHandler handler) {

        return new CommandLine(main).addSubcommand(CommandLine.HelpCommand.class)
                .addSubcommand(versionCommand)
                .addSubcommand(contextPublishCommand)
                .addSubcommand(contextSubscribeCommand)
                .addSubcommand(contextUnsubscribeCommand)
                .addSubcommand(shellConnectCommand)
                .addSubcommand(contextDisconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(contextExitCommand)
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }
}
