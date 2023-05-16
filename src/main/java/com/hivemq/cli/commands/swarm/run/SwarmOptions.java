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

package com.hivemq.cli.commands.swarm.run;

import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;

public class SwarmOptions {

    @CommandLine.Option(names = {"-url"},
                        defaultValue = "http://localhost:8080",
                        description = "The URL of the HiveMQ Swarm REST API endpoint (default: http://localhost:8080)")
    private @NotNull String commanderUrl = "http://localhost:8080";

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-l"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)")
    private void initLogging(final boolean logToLogfile) {
        LoggerUtils.turnOffConsoleLogging(logToLogfile);
    }

    public SwarmOptions() {
    }

    @VisibleForTesting
    public SwarmOptions(final @NotNull String commanderUrl) {
        this.commanderUrl = commanderUrl;
    }

    public @NotNull String getCommanderUrl() {
        return commanderUrl;
    }

    @Override
    public @NotNull String toString() {
        return "SwarmOptions{" + "url='" + commanderUrl + '\'' + '}';
    }
}
