package com.hivemq.cli.cli;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.commands.shell.Shell;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import com.hivemq.cli.ioc.MqttClientModule;
import picocli.CommandLine;

public class HmqCli {

    public static boolean executeCommand(AbstractCommand subCommand) {
        try {
            //AbstractCommand subCommand = subCommandLine.getCommand();
            Injector injector = Guice.createInjector(new MqttClientModule(), new AbstractModule() {
                @Override
                protected void configure() {
                    bind(subCommand.getType()).toInstance(subCommand);
                    bind(AbstractCommand.class).toInstance(subCommand);
                }
            });

            if (subCommand instanceof Subscribe) {
                MqttAction action = injector.getInstance(SubscriptionImpl.class);
                action.run();
            }

            if (subCommand instanceof Shell) {
                ((Shell) subCommand).run();
            }

        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.err);
        } catch (Exception others) {
            // suppress classname
            System.err.println(others.getCause().getMessage());
        }
        return false;
    }

}
