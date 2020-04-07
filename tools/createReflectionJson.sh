#!/usr/bin/env bash
java -cp ././../build/libs/mqtt-cli-1.2.0.jar picocli.codegen.aot.graalvm.ReflectionConfigGenerator \
com.hivemq.cli.commands.shell.ContextPublishCommand \
com.hivemq.cli.commands.shell.ClearScreenCommand \
com.hivemq.cli.commands.shell.ContextDisconnectCommand \
com.hivemq.cli.commands.shell.ContextExitCommand \
com.hivemq.cli.commands.shell.ContextPublishCommand \
com.hivemq.cli.commands.shell.ContextSubscribeCommand \
com.hivemq.cli.commands.shell.ContextSwitchCommand \
com.hivemq.cli.commands.shell.ContextUnsubscribeCommand \
com.hivemq.cli.commands.shell.ListClientsCommand \
com.hivemq.cli.commands.shell.ShellCommand \
com.hivemq.cli.commands.shell.ShellConnectCommand \
com.hivemq.cli.commands.shell.ShellContextCommand \
com.hivemq.cli.commands.shell.VersionCommand \
com.hivemq.cli.commands.shell.ShellDisconnectCommand \
com.hivemq.cli.commands.shell.ShellExitCommand \
com.hivemq.cli.commands.cli.PublishCommand \
com.hivemq.cli.commands.cli.SubscribeCommand \
com.hivemq.cli.commands.MqttCLICommand \
> reflection.json

## the methods from the abstract classes in the PublishCommand and SubscribeCommand must be removed!