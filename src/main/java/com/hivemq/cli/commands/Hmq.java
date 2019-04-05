package com.hivemq.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "hmq",
        subcommands = {
                HmqSub.class
        })
public class Hmq {


}
