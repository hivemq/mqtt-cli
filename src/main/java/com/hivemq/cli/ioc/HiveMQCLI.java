package com.hivemq.cli.ioc;

import dagger.Component;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@Component(modules = {CommandLineModule.class})
public interface HiveMQCLI {
    @NotNull CommandLine commandLine();
}
