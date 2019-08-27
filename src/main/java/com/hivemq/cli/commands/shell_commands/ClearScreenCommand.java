package com.hivemq.cli.commands.shell_commands;

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


    public Void call() throws IOException {

        ShellCommand.clearScreen();
        return null;
    }

    @Override
    public String toString() {
        return "ClearScreen::";
    }
}