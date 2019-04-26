package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command
public abstract class AbstractCommand implements CliCommand {
    @CommandLine.Option(names = {"-?", "--help", "-help"}, usageHelp = true, description = "Display this help and exit.")
    private boolean help;

}
