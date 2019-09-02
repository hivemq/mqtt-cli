package com.hivemq.cli.commands.shell;


import com.hivemq.cli.commands.CliCommand;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "exit",
        description = "Exit the shell")

public class ShellExitCommand implements Runnable, CliCommand {

    @Inject
    public ShellExitCommand() {
    }

    @Override
    public void run() {
        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        ShellCommand.exitShell();
    }

    @Override
    public String toString() {
        return "ShellExit::";
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

