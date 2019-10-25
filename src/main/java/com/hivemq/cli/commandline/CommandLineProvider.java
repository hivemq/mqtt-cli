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
package com.hivemq.cli.commandline;

import com.hivemq.cli.ioc.DaggerContextCommandLine;
import com.hivemq.cli.ioc.DaggerMqttCLI;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class CommandLineProvider {

    private final static CommandLine.Help.ColorScheme COLOR_SCHEME = new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.Ansi.ON)
            .commands(CommandLine.Help.Ansi.Style.bold, CommandLine.Help.Ansi.Style.fg_yellow)
            .options(CommandLine.Help.Ansi.Style.italic, CommandLine.Help.Ansi.Style.fg_yellow)
            .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
            .optionParams(CommandLine.Help.Ansi.Style.italic)
            .build();

    private static final int CLI_WIDTH = 160;

    @NotNull public static CommandLine provideCommandLine() {
        final CommandLine cmd = DaggerMqttCLI.create().commandLine();
        cmd.setColorScheme(COLOR_SCHEME)
                .setUsageHelpWidth(CLI_WIDTH)
                .setParameterExceptionHandler(new CommandErrorMessageHandler());
        return cmd;
    }

    @NotNull public static CommandLine provideContextCommandLine() {
        final CommandLine shellCmd = DaggerContextCommandLine.create().contextCommandLine();
        shellCmd.setColorScheme(COLOR_SCHEME)
                .setUsageHelpWidth(CLI_WIDTH)
                .setParameterExceptionHandler(new ShellErrorMessageHandler());
        return shellCmd;
    }

    @NotNull public static CommandLine.Help.ColorScheme getColorScheme() {
        return COLOR_SCHEME;
    }

    public static int getCliWidth() {
        return CLI_WIDTH;
    }
}
