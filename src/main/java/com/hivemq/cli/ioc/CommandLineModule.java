package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.*;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Georg Held
 */
@Module
public class CommandLineModule {

    @Singleton
    @Provides
    static @NotNull CommandLine provideCommandLine(final @NotNull HiveMQCLICommand main,
                                                   final @NotNull @Named("shell-sub-command") CommandLine shellSubCommand,
                                                   final @NotNull SubscribeCommand subscribeCommand,
                                                   final @NotNull PublishCommand publishCommand) {

        return new CommandLine(main)
                .addSubcommand(shellSubCommand)
                .addSubcommand(subscribeCommand)
                .addSubcommand(publishCommand);
    }
}
