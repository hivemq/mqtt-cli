package com.hivemq.cli.commands;

import com.hivemq.cli.commands.shell.Shell;
import picocli.CommandLine;


@CommandLine.Command(name = "mqtt",
        subcommands = {
                Subscribe.class, Connect.class, Shell.class
        },
        description = "HiveMQ MQTT Command Line Interpreter.")
public class Mqtt {

}
