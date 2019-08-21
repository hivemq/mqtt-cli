package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttVersionConverter;
import com.hivemq.client.mqtt.MqttVersion;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

@CommandLine.Command
public abstract class MqttCommand extends AbstractCommand implements CliCommand {

    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5", converter = MqttVersionConverter.class, description = "The mqtt version used by the client.")
    private MqttVersion version;

    //TODO Implement
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "true", description = "Enable debug mode.")
    private boolean debug;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The host of the message broker..")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker.")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String.")
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

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
