package com.hivemq.cli.commands.shell_commands;

import com.hivemq.cli.commands.CliCommand;
import org.pmw.tinylog.Logger;
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

public class ClearScreenCommand implements CliCommand, Callable<Void> {

    @Inject
    ClearScreenCommand() {
    }


    public Void call() throws IOException {

        if (isVerbose()) {
            Logger.trace("Command: {}", this);
        }

        ShellCommand.clearScreen();
        return null;
    }

    @Override
    public String toString() {
        return "ClearScreen::";
    }

    @Override
    public Class getType() {
        return ShellCommand.class;
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }
}