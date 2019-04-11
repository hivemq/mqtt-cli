package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "con", description = "Connects an mqtt client")
public class Connect extends AbstractCommand {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;


    @Override
    public Class getType() {
        return Connect.class;
    }

}
