package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.cli_commands.DisconnectCommand;
import com.hivemq.cli.commands.shell_commands.ClearScreenCommand;
import com.hivemq.cli.commands.shell_commands.ContextSwitchCommand;
import com.hivemq.cli.commands.shell_commands.ShellCommand;
import com.hivemq.cli.commands.shell_commands.ShellConnectCommand;
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
                                                    final @NotNull ListClientsCommand listClientsCommand) {

        return new CommandLine(shellCommand)
                .addSubcommand(shellConnectCommand)
                .addSubcommand(disconnectCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand);
    }
}
