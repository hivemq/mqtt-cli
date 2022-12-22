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

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.CommanderStateResponse;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.StopRunRequest;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.PrintStream;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "stop",
                     description = "Stop HiveMQ Swarm runs.",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SwarmRunStopCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-r", "--run-id"},
                        description = "The id of the run to stop. If none is given the current run is stopped.")
    private @Nullable Integer runId;

    @CommandLine.Mixin
    private @NotNull SwarmOptions swarmOptions = new SwarmOptions();

    private final @NotNull RunsApi runsApi;
    private final @NotNull CommanderApi commanderApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;
    private final @NotNull PrintStream out;

    @Inject
    public SwarmRunStopCommand(
            final @NotNull Provider<RunsApi> runsApi,
            final @NotNull Provider<CommanderApi> commanderApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull PrintStream out) {
        this.runsApi = runsApi.get();
        this.commanderApi = commanderApi.get();
        this.errorTransformer = errorTransformer;
        this.out = out;
    }

    @VisibleForTesting
    SwarmRunStopCommand(
            final @NotNull String commanderUrl,
            final @Nullable Integer runId,
            final @NotNull RunsApi runsApi,
            final @NotNull CommanderApi commanderApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull PrintStream out) {
        this(() -> runsApi, () -> commanderApi, errorTransformer, out);
        this.swarmOptions = new SwarmOptions(commanderUrl);
        this.runId = runId;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        // Check if given URL is valid
        final HttpUrl httpUrl = HttpUrl.parse(swarmOptions.getCommanderUrl());
        if (httpUrl == null) {
            Logger.error("URL is not in a valid format: {}", swarmOptions.getCommanderUrl());
            System.err.println("URL is not in a valid format: " + swarmOptions.getCommanderUrl());
            return -1;
        }

        runsApi.getApiClient().setBasePath(swarmOptions.getCommanderUrl());

        final int usedRunID;
        if (runId == null) {
            final CommanderStateResponse commanderStatus;
            try {
                commanderStatus = commanderApi.getCommanderStatus();
            } catch (final ApiException apiException) {
                final Error error = errorTransformer.transformError(apiException);
                Logger.error("Could not obtain current run. {}", error.getDetail());
                System.err.println("Could not obtain current run. " + error.getDetail());
                return -1;
            }
            if (commanderStatus.getRunId() == null) {
                out.println("No run in progress.");
                return 0;
            } else {
                usedRunID = Integer.parseInt(commanderStatus.getRunId());
            }
        } else {
            usedRunID = runId;
        }

        try {
            final StopRunRequest stopRunRequest = new StopRunRequest();
            stopRunRequest.runStatus("STOPPING");
            runsApi.stopRun(Integer.toString(usedRunID), stopRunRequest);
            return 0;
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Failed to stop run '{}'. {}.", usedRunID, error.getDetail());
            System.err.println("Failed to stop run '" + usedRunID + "'. " + error.getDetail());
            return -1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "SwarmRunStopCommand{" +
                "runId=" +
                runId +
                ", swarmOptions=" +
                swarmOptions +
                ", runsApi=" +
                runsApi +
                ", commanderApi=" +
                commanderApi +
                ", errorTransformer=" +
                errorTransformer +
                ", out=" +
                out +
                '}';
    }
}
