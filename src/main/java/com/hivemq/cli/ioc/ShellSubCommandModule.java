package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.ClearScreenCommand;
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
                                                    final @NotNull ConnectCommand connectCommand,
                                                    final @NotNull SubscribeCommand subscribeCommand,
                                                    final @NotNull PublishCommand publishCommand,
                                                    final @NotNull DisconnectCommand disconnectCommand,
                                                    final @NotNull ClearScreenCommand clearScreenCommand,
                                                    final @NotNull ListClientsCommand listClientsCommand) {

        return new CommandLine(shellCommand)
                .addSubcommand(connectCommand)
                .addSubcommand(subscribeCommand)
                .addSubcommand(publishCommand)
                .addSubcommand(disconnectCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(listClientsCommand);
    }
}
