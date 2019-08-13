package com.hivemq.cli.cli;

import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.commands.Publish;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.SubscriptionImpl;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;

public class HmqCli {

    private final SubscriptionImpl subscription;
    private final ConnectionImpl connection;

    @Inject
    public HmqCli(final @NotNull SubscriptionImpl subscription, final @NotNull ConnectionImpl connection) {
        this.subscription = subscription;
        this.connection = connection;
    }

    public short executeCommand(AbstractCommand subCommand) {
        try {
            if (subCommand instanceof Subscribe) {
                subscription.setParam((Subscribe) subCommand);
                subscription.run();
               // SubscriptionImpl.get((Subscribe) subCommand).run();
            } else if (subCommand instanceof Publish) {
                ((Publish) subCommand).run();
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
