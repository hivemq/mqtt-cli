package com.hivemq.cli.ioc;

import com.hivemq.cli.commands.Subscribe;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

/**
 * @author Georg Held
 */
@Module
public class CommandLineModule {

    @Provides
    @NotNull CommandLine provideCommandLine(final @NotNull Subscribe subscribe) {
        return new CommandLine(subscribe);
    }
}
