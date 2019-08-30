package com.hivemq.cli.commands.cli;

import com.hivemq.cli.commands.CliCommand;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import picocli.CommandLine;

@CommandLine.Command(sortOptions = false,
        synopsisHeading = "%n@|bold Usage|@:  ",
        synopsisSubcommandLabel = "{ pub | sub | shell }",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options|@:%n",
        commandListHeading = "%n@|boldCommands|@:%n",
        separator = " ")
public abstract class AbstractCommand implements CliCommand {

    private boolean debug;
    private boolean verbose;

    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode")
    private void activateDebugMode(final boolean debug) {

        if (debug && !verbose) {
            this.debug = true;
            Configurator.currentConfig().level(Level.DEBUG).activate();
        }
    }

    @CommandLine.Option(names = {"-v", "--verbose"}, defaultValue = "false", description = "Enable verbose mode")
    private void activateVerboseMode(final boolean verbose) {

        if (verbose) {
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
