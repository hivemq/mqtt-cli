package com.hivemq.cli;

import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "mqtt",
        subcommands = {
                Subscribe.class, Connect.class, Shell.class
        },
        description = "HiveMQ MQTT Command Line Interpreter.")
public class Mqtt {

    public Mqtt() {
    }

    public static void main(String[] args) {

        com.hivemq.cli.Mqtt mqtt = new com.hivemq.cli.Mqtt();

        final CommandLine cmd = new CommandLine(mqtt);

        try {
            List<CommandLine> parse = cmd.parse(args);
            if (parse.size() > 1) {
                CommandLine subCommandLine = parse.get(1);
                CommandLine.printHelpIfRequested(cmd.parseArgs(args));
                HmqCli.executeCommand(subCommandLine.getCommand());
            } else {
                parse.get(0).usage(System.err);
            }
        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());

        }
    }
}
