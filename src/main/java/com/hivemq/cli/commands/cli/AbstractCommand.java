/*
 * Copyright 2019 dc-square and the HiveMQ Commandline Interface Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
