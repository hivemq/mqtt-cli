/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.cli.commandline;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CommandLineConfig {

    private final static @NotNull CommandLine.Help.ColorScheme COLOR_SCHEME =
            new CommandLine.Help.ColorScheme.Builder(CommandLine.Help.Ansi.AUTO).commands(CommandLine.Help.Ansi.Style.bold,
                            CommandLine.Help.Ansi.Style.fg_yellow)
                    .options(CommandLine.Help.Ansi.Style.italic, CommandLine.Help.Ansi.Style.fg_yellow)
                    .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
                    .optionParams(CommandLine.Help.Ansi.Style.italic)
                    .build();
    private static final int CLI_WIDTH = 160;

    @Inject
    public CommandLineConfig() {
    }

    public @NotNull CommandLine.Help.ColorScheme getColorScheme() {
        return COLOR_SCHEME;
    }

    public int getCliWidth() {
        return CLI_WIDTH;
    }
}
