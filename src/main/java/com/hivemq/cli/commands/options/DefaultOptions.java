package com.hivemq.cli.commands.options;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

/**
 * Helper Option in order to allow the --version and --help option at commands which already define -h and -V
 * themselves.
 */
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
