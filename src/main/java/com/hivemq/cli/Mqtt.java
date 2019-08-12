package com.hivemq.cli;

import com.hivemq.cli.cli.HmqCli;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.writers.RollingFileWriter;
import picocli.CommandLine;

import java.security.Security;
import java.util.List;

@CommandLine.Command(name = "mqtt",
        subcommands = {
                Subscribe.class, Publish.class, Shell.class
        },
        description = "HiveMQ MQTT Command Line Interpreter.")
public class Mqtt {
    final static int EXIT_SUCCESS = 0;
    final static int EXIT_FAIL = -1;
    @CommandLine.Option(names = {"-?", "--help"}, usageHelp = true, description = "Display this help and exit.")
    private boolean help;

    /**
     * Main class that starts a commandline interface to process ONE command - see help
     *
     * @param args
     */
    public static void main(String[] args) {

        final com.hivemq.cli.Mqtt mqtt = new com.hivemq.cli.Mqtt();
        final CommandLine cmd = new CommandLine(mqtt);
        final HmqCli hmqCli = new HmqCli();
        short status = EXIT_FAIL;
        Security.setProperty("crypto.policy", "unlimited");

        Configurator.defaultConfig()
                .writer(new RollingFileWriter("hmq-mqtt-log.txt", 30, false, new TimestampLabeler("yyyy-MM-dd"), new SizePolicy(1024 * 10)))
                .formatPattern("{date:yyyy-MM-dd HH:mm:ss}: {{level}:|min-size=6} {context:identifier}: {message}")
                .level(Level.DEBUG)
                .activate();
        try {
            final List<CommandLine> parse = cmd.parse(args);
            if (parse.size() > 1) {
                CommandLine subCommandLine = parse.get(1);
                boolean helpRequested = CommandLine.printHelpIfRequested(subCommandLine.getParseResult());
                if (!helpRequested) {
                    if (subCommandLine.getCommand() instanceof Subscribe) {
                        // subscribe was called directly - not in interactive shell mode
                        Subscribe sub = subCommandLine.getCommand();
                        sub.setPrintToSTDOUT(true); // default print to stdout if subscribe is waiting in console
                    }

                    status = hmqCli.executeCommand(subCommandLine.getCommand());

                    if (status == EXIT_SUCCESS &&
                            subCommandLine.getCommand() instanceof Subscribe) {
                        stayConnected(subCommandLine.getCommand());
                    }
                }
            } else {
                parse.get(0).usage(System.err);
            }
        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            Logger.error(ex.getMessage());
        }

        System.exit(status);
    }

    private static void stayConnected(Subscribe subscribeCommand) {
        // stay in subscriber mode until interrupted by user
        try {
            subscribeCommand.stay();
        } catch (InterruptedException e) {
            // we stop processing the subscription
        }
    }
}
