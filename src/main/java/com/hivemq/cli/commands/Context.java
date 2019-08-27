package com.hivemq.cli.commands;

public interface Context {

    boolean isDebug();

    boolean isVerbose();

    String getIdentifier();

    String getKey();
}
