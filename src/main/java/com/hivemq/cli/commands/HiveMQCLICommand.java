package com.hivemq.cli.commands;

import com.hivemq.cli.ioc.HiveMQCLI;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "hivemq-cli",
        description = "HiveMQ MQTT Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        synopsisSubcommandLabel = "{ pub | sub | shell }",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        version = HiveMQCLICommand.VERSION_STRING)

public class HiveMQCLICommand {

    public static final @NotNull String VERSION_STRING = "1.0";

    @Inject
    HiveMQCLICommand() {
    }

}
