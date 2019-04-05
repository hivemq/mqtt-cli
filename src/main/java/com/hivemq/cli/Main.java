package com.hivemq.cli;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.cli.cli.Cli;
import com.hivemq.cli.commands.AbstractCommand;
import com.hivemq.cli.commands.HmqSub;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import com.hivemq.cli.ioc.MqttClientModule;

public class Main {

    public static void main(String[] args) {


        final AbstractCommand subCommand = Cli.getCliCommandOrDie();

        Injector injector = Guice.createInjector(new MqttClientModule(), new AbstractModule() {

            @Override
            protected void configure() {
                bind(subCommand.getType()).toInstance(subCommand);
                bind(AbstractCommand.class).toInstance(subCommand);
            }
        });

        MqttAction action = null;
        if (subCommand instanceof HmqSub) {

            action = injector.getInstance(SubscriptionImpl.class);
        }
        action.run();

    }
}
