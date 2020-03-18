#!/usr/bin/env bash
java -cp ././../build/libs/mqtt-cli-1.1.2.jar picocli.codegen.aot.graalvm.ReflectionConfigGenerator \
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
> reflection_pico.json


# Add additional mappings (currently, only HelpCommand for shell mode)
# jq filter from https://stackoverflow.com/questions/42011086/merge-arrays-of-json requires jq >= 1.5
# the methods from the abstract classes in the PublishCommand and SubscribeCommand must be removed,
# the second query in the pipeline does just that (remove methods array for classes that contain the activateDebugMethod method mapping, which is invalid)
jq -s '.[0]=([.[]]|flatten)|.[0]' reflection_pico.json reflection_additional.json \
  | jq 'del(.[].methods | select(. != null) | select(.[].name == "activateDebugMode"))' \
  > reflection.json