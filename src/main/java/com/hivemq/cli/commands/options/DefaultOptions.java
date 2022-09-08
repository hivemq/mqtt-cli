package com.hivemq.cli.commands.options;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class DefaultOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--version"}, versionHelp = true, description = "display version info")
    private boolean versionInfoRequested;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @Override
    public @NotNull String toString() {
        return "DefaultOptions{" + "versionInfoRequested=" + versionInfoRequested + ", usageHelpRequested=" +
                usageHelpRequested + '}';
    }
}
