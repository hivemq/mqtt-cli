package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command
public abstract class MqttCommand extends AbstractCommand implements CliCommand {

    static final int DEFAULT_MQTT_PORT = 1883;
    static final int DEFAULT_MQTT_SSL_PORT = 8883;

    //TODO Implement complete
    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5", description = "The mqtt version used by the client.")
    private int version;

    //TODO Implement
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "true", description = "Enable debug mode.")
    private boolean debug;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The host of the message broker..")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883", description = "The port of the message broker.")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String.")
    private String identifier;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
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
