package com.hivemq.cli.commands.shell_commands;

import com.hivemq.cli.commands.Context;
import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;

@CommandLine.Command(name = "switch",
        description = "switch the current context")
public class ContextSwitchCommand extends ShellContextCommand implements Runnable, Context {

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "The name of the context, e.g. client@localhost")
    private String contextName;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    private String identifier;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    private String host;


    @Inject
    public ContextSwitchCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }


    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        if (contextName != null) {
            try {
                extractKeyFromContextName(contextName);
            } catch (IllegalArgumentException ex) {
                if (isVerbose()) {
                    Logger.error(ex);
                }
                Logger.error(ex.getMessage());
                return;
            }
        }

        if (identifier == null) {
            ShellCommand.usage(this);
            return;
        }

        MqttClient client = mqttClientExecutor.getMqttClientFromCache(this);

        if (client != null) {
            updateContext(client);
        } else {
            if (isDebug()) {
                Logger.debug("Client with key: {} not in Cache", getKey());
            }
            Logger.error("Context {}@{} not found", identifier, host);
        }
    }

    private void extractKeyFromContextName(String contextName) {
        String[] context = contextName.split("@");

        if (context.length == 1) {
            identifier = context[0];
        } else if (context.length == 2) {
            identifier = context[0];
            host = context[1];
        } else {
            throw new IllegalArgumentException("Context name is not valid: " + contextName);
        }
    }

    @Override
    public String getKey() {
        return "client {" +
                "identifier='" + getIdentifier() + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "ContextSwitch:: {" +
                "contextName='" + contextName + '\'' +
                ", key=" + getKey() +
                '}';
    }

    @Override
    public Class getType() {
        return ContextSwitchCommand.class;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }
}
