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
import java.io.PrintWriter;

public class ShellErrorMessageHandler extends CommonErrorMessageHandler
        implements CommandLine.IParameterExceptionHandler {

    @Inject
    ShellErrorMessageHandler() {
    }

    @Override
    public int handleParseException(
            final @NotNull CommandLine.ParameterException ex, final @NotNull String @NotNull [] args) throws Exception {
        final int exitCode = super.handleParseException(ex, args);
        final PrintWriter writer = ex.getCommandLine().getErr();

        if (ex instanceof CommandLine.UnmatchedArgumentException &&
                ((CommandLine.UnmatchedArgumentException) ex).getUnmatched().get(0).equals(args[0])) {
            writer.printf("Try 'help' to get a list of commands.%n");
        } else {
            writer.printf("Try 'help %s' for more information.%n", args[0]);
        }

        return exitCode;
    }
}
