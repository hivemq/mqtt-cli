package com.hivemq.cli.commands;

interface CliCommand<T extends CliCommand> {

    Class<T> getType();


}
