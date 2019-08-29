package com.hivemq.cli.commands.shell_commands;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.LoggingContext;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(sortOptions = false,
        name = "> ",
        description = "In context mode all mqtt commands relate to the currently active client.",
        synopsisHeading = "%n@|bold Usage|@:  ",
        synopsisSubcommandLabel = "{ pub | sub | dis | exit | switch | ls | cls }",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options|@:%n",
        commandListHeading = "%n@|bold Commands|@:%n",
        separator = " ")

public class ShellContextCommand implements Runnable, CliCommand {

    public static @Nullable MqttClient contextClient;
    MqttClientExecutor mqttClientExecutor;

    @Inject
    public ShellContextCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }


    static void updateContext(final @Nullable MqttClient client) {
        if (client != null && client.getConfig().getState().isConnectedOrReconnect()) {
            if (ShellCommand.isVerbose()) {
                Logger.trace("Update context to {}@{}", client.getConfig().getClientIdentifier().get(), client.getConfig().getServerHost());
            }
            LoggingContext.put("identifier", client.getConfig().getClientIdentifier().get().toString());
            contextClient = client;
            ShellCommand.readFromContext();
        }
    }

    static void removeContext() {
        if (ShellCommand.isVerbose()) {
            Logger.trace("Remove context");
        }
        contextClient = null;
        ShellCommand.readFromShell();
    }

    @Override
    public void run() {
        System.out.println(ShellCommand.getUsageMessage());
    }

    public String getKey() {
        return "client {" +
                "identifier='" + contextClient.getConfig().getClientIdentifier().get() + '\'' +
                ", host='" + contextClient.getConfig().getServerHost() + '\'' +
                '}';
    }

    public String getIdentifier() {
        return contextClient.getConfig().getClientIdentifier().get().toString();
    }

    public boolean isDebug() {
        return ShellCommand.isDebug();
    }

    @Override
    public Class getType() {
        return ShellContextCommand.class;
    }

    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }
}
