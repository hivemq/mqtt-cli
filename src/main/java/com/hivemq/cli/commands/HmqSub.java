package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "sub")
public class HmqSub extends AbstractCommand {


    @Override
    public Class getType() {
        return HmqSub.class;
    }
}
