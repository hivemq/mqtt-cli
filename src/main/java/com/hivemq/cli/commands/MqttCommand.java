package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

@CommandLine.Command()
public abstract class MqttCommand extends AbstractCommand implements CliCommand {


    @CommandLine.Option(names = {"-V", "--version"}, defaultValue = "5", converter = MqttVersionConverter.class, description = "The mqtt version used by the client (default: 5)")
    private MqttVersion version;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker (default: 1883)")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    @Nullable
    private String identifier;

    public @NotNull MqttVersion getVersion() {
        return version;
    }

    public void setVersion(final @NotNull MqttVersion version) {
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final @Nullable String identifier) {
        this.identifier = identifier;
    }

}
