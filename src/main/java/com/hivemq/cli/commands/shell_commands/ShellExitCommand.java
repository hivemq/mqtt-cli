package com.hivemq.cli.commands.shell_commands;


import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
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
    public Class getType() {
        return ShellExitCommand.class;
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

