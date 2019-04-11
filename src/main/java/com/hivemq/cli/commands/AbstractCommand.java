package com.hivemq.cli.commands;

import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command
public abstract class AbstractCommand implements CliCommand {

    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5")
    private int version;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost")
    private String host;

    @CommandLine.Option(names = {"-p", "--port"}, defaultValue = "1883")
    private int port;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The Client Identifier UTF-8 String.")
    private String identifier;

    public String getIdentifier() {
        return identifier != null ? identifier : "hmq" + version + "-" + UUID.randomUUID().toString();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getVersion() {
        return version;
    }


}
