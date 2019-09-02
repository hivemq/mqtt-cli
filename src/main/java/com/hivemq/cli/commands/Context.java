package com.hivemq.cli.commands;

public interface Context extends CliCommand {

    String getIdentifier();

    String getKey();
}
