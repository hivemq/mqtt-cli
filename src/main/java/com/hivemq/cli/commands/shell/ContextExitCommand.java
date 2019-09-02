package com.hivemq.cli.commands.shell;


import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "exit",
        description = "Exit the current context")
public class ContextExitCommand extends ShellContextCommand implements Runnable {

    @Inject
    public ContextExitCommand(@NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @Override
    public void run() {
        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        removeContext();
    }

    @Override
    public String toString() {
        return "ContextExit::";
    }

}
