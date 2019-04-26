package com.hivemq.cli.cli;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.SubscriptionImpl;
import picocli.CommandLine;

public class HmqCli {

    public short executeCommand(AbstractCommand subCommand) {
        try {
            if (subCommand instanceof Subscribe) {
                SubscriptionImpl.get((Subscribe) subCommand).run();
            } else if (subCommand instanceof Connect) {
                ConnectionImpl.get((Connect) subCommand).run();
            } else if (subCommand instanceof Shell) {
                ((Shell) subCommand).run();
            }
            return 0;

        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
        } catch (Exception others) {
            // suppress classname in output console
            System.err.println(others.getCause().getMessage());
        }
        return 1;
    }

}
