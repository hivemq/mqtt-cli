package com.hivemq.cli.commands.shell_commands;

import com.hivemq.cli.commands.Context;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "dis",
        aliases = "disconnect",
        description = "Disconnects this mqtt client")

public class ContextDisconnectCommand extends ShellContextCommand implements Runnable, Context {

    @Inject
    public ContextDisconnectCommand(final @NotNull MqttClientExecutor executor) {
        super(executor);
    }

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            mqttClientExecutor.disconnect(this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
        }

        removeContext();

    }

    @Override
    public String toString() {
        return "ContextDisconnect:: {" +
                "key=" + getKey() +
                "}";
    }

    @Override
    public Class getType() {
        return ContextDisconnectCommand.class;
    }
}
