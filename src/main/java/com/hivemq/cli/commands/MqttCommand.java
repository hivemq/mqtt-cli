package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command
public abstract class MqttCommand extends AbstractCommand implements CliCommand {

    //TODO Implement complete
    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5", description = "The mqtt version used by the client.")
    private int version;

    //TODO Implement
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode.")
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

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isDebug() {
        return debug;
    }

}
