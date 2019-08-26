package com.hivemq.cli.commands;

import com.hivemq.cli.commands.ShellCommand;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Callable;


/**
 * Command that clears the screen.
 */
@CommandLine.Command(
        name = "cls",
        aliases = "clear",
        mixinStandardHelpOptions = true,
        description = "Clears the screen")

public class ClearScreenCommand implements Callable<Void> {

    @Inject
    ClearScreenCommand() {
    }

    @CommandLine.ParentCommand
    ShellCommand parent;

    public Void call() throws IOException {
        parent.reader.clearScreen();
        return null;
    }
}