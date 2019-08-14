package com.hivemq.cli.commands;

import com.hivemq.cli.ioc.HiveMQCLI;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "hivemq-cli", description = "HiveMQ MQTT Command Line Interpreter.", mixinStandardHelpOptions = true, version = HiveMQCLICommand.VERSION_STRING)
public class HiveMQCLICommand {

    public static final @NotNull String VERSION_STRING = "1.0";

    @Inject
    HiveMQCLICommand() {
    }

}
