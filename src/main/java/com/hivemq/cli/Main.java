package com.hivemq.cli;

import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.commands.Mqtt;
import picocli.CommandLine;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Mqtt mqtt = new Mqtt();

        final CommandLine cmd = new CommandLine(mqtt);
        cmd.usage(System.out);

        try {
            List<CommandLine> parse = cmd.parse(args);
            if (parse.size() > 1) {
                CommandLine subCommandLine = parse.get(1);
                CommandLine.printHelpIfRequested(cmd.parseArgs(args));
                HmqCli.executeCommand( subCommandLine.getCommand());
            } else {
                parse.get(0).usage(System.err);
            }
        } catch (Exception all) {
            System.err.println(all);
        }
    }


}
