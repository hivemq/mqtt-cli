package com.hivemq.cli;

import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Disconnect;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "mqtt",
        subcommands = {
                Subscribe.class, Publish.class, Shell.class
        },
        description = "HiveMQ MQTT Command Line Interpreter.")
public class Mqtt {
    @CommandLine.Option(names = {"-?", "--help"}, usageHelp = true, description = "Display this help and exit.")
    private boolean help;

    public static void main(String[] args) {

        final com.hivemq.cli.Mqtt mqtt = new com.hivemq.cli.Mqtt();
        final CommandLine cmd = new CommandLine(mqtt);
        final HmqCli hmqCli = new HmqCli();
        short status = 1;

        try {
            final List<CommandLine> parse = cmd.parse(args);
            if (parse.size() > 1) {
                CommandLine subCommandLine = parse.get(1);
                boolean helpRequested = CommandLine.printHelpIfRequested(subCommandLine.getParseResult());
                if (!helpRequested) {
                    status = hmqCli.executeCommand(subCommandLine.getCommand());
                }
            } else {
                parse.get(0).usage(System.err);
            }
        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
        }
        System.exit(status);
    }
}
