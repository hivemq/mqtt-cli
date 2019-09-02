package com.hivemq.cli.ioc;

import dagger.Component;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        ContextCommandModule.class
})
public interface ContextCommandLine {

    @NotNull CommandLine contextCommandLine();

}
