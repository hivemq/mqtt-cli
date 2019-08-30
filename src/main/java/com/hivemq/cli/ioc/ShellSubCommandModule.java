package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.cli.DisconnectCommand;
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
                                                    final @NotNull ShellConnectCommand shellConnectCommand,
                                                    final @NotNull DisconnectCommand disconnectCommand,
                                                    final @NotNull ContextSwitchCommand contextSwitchCommand,
                                                    final @NotNull ClearScreenCommand clearScreenCommand,
                                                    final @NotNull ListClientsCommand listClientsCommand,
                                                    final @NotNull ShellExitCommand shellExitCommand) {

        return new CommandLine(shellCommand)
                .addSubcommand(shellConnectCommand)
                .addSubcommand(disconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(shellExitCommand);
    }
}
