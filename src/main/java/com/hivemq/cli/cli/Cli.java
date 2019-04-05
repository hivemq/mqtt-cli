package com.hivemq.cli.cli;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.commands.Hmq;
import picocli.CommandLine;

import java.util.List;

public class Cli {


    public static AbstractCommand getCliCommandOrDie() {

        String[] ar = {"sub","-h","broker.hivemq.com"};

        Hmq hmq = new Hmq();

        final CommandLine cmd = new CommandLine(hmq);

        try {
            List<CommandLine> parse = cmd.parse(ar);
            if (cmd.isUsageHelpRequested()) {
                cmd.usage(System.out);
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
            }
            return parse.get(1).getCommand();
        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }
        } catch (Exception ex) {
            throw new CommandLine.ExecutionException(cmd, "Error while calling " + hmq, ex);
        }
        System.exit(0);
        return null;
    }

}
