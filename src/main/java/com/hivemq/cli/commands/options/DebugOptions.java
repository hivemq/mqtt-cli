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

package com.hivemq.cli.commands.options;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

public class DebugOptions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-d", "--debug"}, defaultValue = "false", description = "Enable debug mode")
    private void activateDebugMode(final boolean debug) {
        if (debug && !isVerbose) {
            isDebug = true;
        }
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-v", "--verbose"}, defaultValue = "false", description = "Enable verbose mode")
    private void activateVerboseMode(final boolean verbose) {
        if (verbose) {
            isVerbose = true;
            isDebug = true;
        } else {
            isVerbose = false;
        }
    }

    private boolean isDebug;
    private boolean isVerbose;

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    @Override
    public @NotNull String toString() {
        return "DebugOptions{" + "isDebug=" + isDebug + ", isVerbose=" + isVerbose + '}';
    }
}
