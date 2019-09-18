/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
package com.hivemq.cli.commands.shell;

import com.hivemq.cli.HiveMQCLIMain;
import com.hivemq.cli.commands.CliCommand;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Callable;


/**
 * Command that clears the screen.
 */
@CommandLine.Command(
        name = "cls",
        aliases = "clear",
        description = "Clears the screen")

public class ClearScreenCommand implements CliCommand, Callable<Void> {

    @Inject
    ClearScreenCommand() {
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    public Void call() throws IOException {

        if (isVerbose()) {
            Logger.trace("Command: {}", this);
        }

        ShellCommand.clearScreen();
        return null;
    }

    @Override
    public String toString() {
        return "ClearScreen::";
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }
}