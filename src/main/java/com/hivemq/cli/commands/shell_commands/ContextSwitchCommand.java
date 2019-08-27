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

    @CommandLine.Option(names = {"-V", "--version"}, defaultValue = "5", converter = MqttVersionConverter.class, description = "The mqtt version used by the client (default: 5)")
    private MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker (default: 1883)")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, required = true, description = "The client identifier UTF-8 String (default randomly generated string)")
    @NotNull
    private String identifier;

    @Inject
    public ContextSwitchCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }


    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        MqttClient client = mqttClientExecutor.getMqttClientFromCache(this);

        if (client != null) {
            updateContext(client);
        } else {
            Logger.error("Client with key {} not found", getKey());
        }
    }

    @Override
    public String getKey() {
        return "client {" +
                "version=" + getVersion() +
                ", host='" + getHost() + '\'' +
                ", port=" + getPort() +
                ", identifier='" + getIdentifier() + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return "ContextSwitch:: {" +
                "key=" + getKey() +
                '}';
    }

    public MqttVersion getVersion() {
        return version;
    }

    public void setVersion(final MqttVersion version) {
        this.version = version;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }
}
