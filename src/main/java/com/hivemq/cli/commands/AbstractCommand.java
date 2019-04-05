package com.hivemq.cli.commands;

import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command
public abstract class AbstractCommand implements CliCommand {

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"})
    private String identifier;


    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }
}
