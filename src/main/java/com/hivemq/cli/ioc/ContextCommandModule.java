package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.*;
import com.hivemq.cli.commands.shell_commands.*;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Singleton;

@Module
public class ContextCommandModule {

    @Singleton
    @Provides
    static @NotNull CommandLine provideCommandLine(final @NotNull ShellContextCommand main,
                                                   final @NotNull ContextPublishCommand contextPublishCommand,
                                                   final @NotNull ContextSubscribeCommand contextSubscribeCommand,
                                                   final @NotNull ContextUnsubscribeCommand contextUnsubscribeCommand,
                                                   final @NotNull ContextDisconnectCommand contextDisconnectCommand,
                                                   final @NotNull ContextSwitchCommand contextSwitchCommand,
                                                   final @NotNull ContextExitCommand contextExitCommand,
                                                   final @NotNull ListClientsCommand listClientsCommand,
                                                   final @NotNull ClearScreenCommand clearScreenCommand) {

        return new CommandLine(main)
                .addSubcommand(contextPublishCommand)
                .addSubcommand(contextSubscribeCommand)
                .addSubcommand(contextUnsubscribeCommand)
                .addSubcommand(contextDisconnectCommand)
                .addSubcommand(contextSwitchCommand)
                .addSubcommand(listClientsCommand)
                .addSubcommand(clearScreenCommand)
                .addSubcommand(contextExitCommand);
    }
}
