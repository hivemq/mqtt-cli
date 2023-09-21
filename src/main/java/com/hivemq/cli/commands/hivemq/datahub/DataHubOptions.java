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

package com.hivemq.cli.commands.hivemq.datahub;

import com.hivemq.cli.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;

public class DataHubOptions {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) // will be initialized via default value
    @CommandLine.Option(names = {"-u", "--url"},
                        defaultValue = "http://localhost:8888",
                        description = "The URL of the HiveMQ REST API endpoint (default http://localhost:8888)",
                        order = 1)
    private @NotNull String url;

    @SuppressWarnings({"unused"})
    @CommandLine.Option(names = {"-r", "--rate"},
                        defaultValue = "1500",
                        description = "The rate limit of the REST calls to the HiveMQ API endpoint in requests per second (default 1500/s)",
                        order = 2)
    private double rateLimit;

    @SuppressWarnings({"unused"})
    @CommandLine.Option(names = {"-l", "--log"},
                        defaultValue = "false",
                        description = "Log to $HOME/.mqtt-cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)",
                        order = 3)
    private void initLogging(final boolean logToLogfile) {
        LoggerUtils.turnOffConsoleLogging(logToLogfile);
    }

    public DataHubOptions() {
    }

    @VisibleForTesting
    public DataHubOptions(final @NotNull String url, final int rateLimit) {
        this.url = url;
        this.rateLimit = rateLimit;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public double getRateLimit() {
        return rateLimit;
    }

    @Override
    public @NotNull String toString() {
        return "DataGovernanceOptions{" + "url='" + url + '\'' + ", rateLimit=" + rateLimit + '}';
    }
}
