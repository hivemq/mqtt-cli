package com.hivemq.cli.commands;

public interface CliCommand<T extends CliCommand> {

    Class<T> getType();


}
