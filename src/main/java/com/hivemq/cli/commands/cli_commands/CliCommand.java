package com.hivemq.cli.commands.cli_commands;

interface CliCommand<T extends CliCommand> {

    Class<T> getType();


}
