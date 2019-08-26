package com.hivemq.cli.commands;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import picocli.CommandLine;

@CommandLine.Command(sortOptions = false,
        synopsisHeading = "%n@|bold Usage|@:  ",
        synopsisSubcommandLabel = "{ pub | sub | shell }",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options|@:%n",
        commandListHeading = "%n@|boldCommands|@:%n",
        separator = " ")
abstract class AbstractCommand implements CliCommand {

    private boolean debug;
    private boolean verbose;

    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode")
    private void activateDebugMode(final boolean debug) {

        if (ShellCommand.IN_SHELL) {
            verbose = ShellCommand.VERBOSE;
            this.debug = ShellCommand.DEBUG;
            if (debug) {
                Logger.warn("-d option is omitted in shell mode. Executing command with default shell debug level.");
            }
        } else if (debug && !verbose) {
            this.debug = true;
            Configurator.currentConfig().level(Level.DEBUG).activate();
        }
    }

    @CommandLine.Option(names = {"-v", "--verbose"}, defaultValue = "false", description = "Enable verbose mode")
    private void activateVerboseMode(final boolean verbose) {

        if (ShellCommand.IN_SHELL) {
            this.verbose = ShellCommand.VERBOSE;
            debug = ShellCommand.DEBUG;
            if (verbose) {
                Logger.warn("-vb option is omitted in shell mode. Executing command with default shell verbose level.");
            }
        } else if (verbose) {
            this.verbose = true;
            debug = true;
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
