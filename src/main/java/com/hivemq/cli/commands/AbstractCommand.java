package com.hivemq.cli.commands;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import picocli.CommandLine;

@CommandLine.Command
abstract class AbstractCommand implements CliCommand {

    private boolean debug;
    private boolean verbose;

    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode.")
    private void activateDebugMode(boolean debug) {

        if (ShellCommand.IN_SHELL) {
            this.verbose = ShellCommand.VERBOSE;
            this.debug = ShellCommand.DEBUG;
            if (debug) {
                Logger.warn("-d option is omitted in shell mode. Executing command with default shell debug level.");
            }
        } else if (debug && !verbose) {
            this.debug = true;
            Configurator.currentConfig().level(Level.DEBUG).activate();
        }
    }

    @CommandLine.Option(names = {"-vb", "--verbose"}, defaultValue = "false", description = "Enable verbose mode.")
    private void activateVerboseMode(boolean verbose) {

        if (ShellCommand.IN_SHELL) {
            this.verbose = ShellCommand.VERBOSE;
            this.debug = ShellCommand.DEBUG;
            if (verbose) {
                Logger.warn("-vb option is omitted in shell mode. Executing command with default shell verbose level.");
            }
        } else if (verbose) {
            this.verbose = true;
            this.debug = true;
            Configurator.currentConfig().level(Level.TRACE).activate();
        } else {
            this.verbose = false;
        }

    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

}
