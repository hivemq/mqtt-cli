package com.hivemq.cli.commands.options;

import picocli.CommandLine;

public class DebugOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode")
    private void activateDebugMode(final boolean debug) {
        if (debug && !isVerbose) {
            this.isDebug = true;
        }
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-v", "--verbose"}, defaultValue = "false", description = "Enable verbose mode")
    private void activateVerboseMode(final boolean verbose) {
        if (verbose) {
            this.isVerbose = true;
            isDebug = true;
        } else {
            this.isVerbose = false;
        }
    }

    private boolean isDebug;
    private boolean isVerbose;

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    @Override
    public String toString() {
        return "DebugOptions{" + "isDebug=" + isDebug + ", isVerbose=" + isVerbose + '}';
    }
}
