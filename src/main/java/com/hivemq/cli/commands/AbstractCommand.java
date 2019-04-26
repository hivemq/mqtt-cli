package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command
public abstract class AbstractCommand implements CliCommand {
    @CommandLine.Option(names = {"-?", "--help", "-help"}, usageHelp = true, description = "Display this help and exit.")
    private boolean help;

    //TODO Implement complete
    @CommandLine.Option(names = {"-v", "--version"}, defaultValue = "5", description = "The mqtt version used by the client.")
    private int version;
    //TODO Implement
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode.")
    private boolean debug;

    public int getVersion() {
        return version;
    }

    public boolean isDebug() {
        return debug;
    }

}
