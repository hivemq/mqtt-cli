package com.hivemq.cli.ioc;

import dagger.Component;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        CommandLineModule.class,
        ShellSubCommandModule.class
})
public interface HiveMQCLI {

    @NotNull CommandLine commandLine();

}
