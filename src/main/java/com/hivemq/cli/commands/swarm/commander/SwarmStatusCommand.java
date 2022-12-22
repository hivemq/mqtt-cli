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

package com.hivemq.cli.commands.swarm.commander;

import com.google.gson.Gson;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.commands.swarm.run.SwarmOptions;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.CommanderStateResponse;
import com.hivemq.cli.openapi.swarm.RunResponse;
import com.hivemq.cli.openapi.swarm.RunsApi;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.PrintStream;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "status",
                     description = "Check the status of HiveMQ Swarm. (READY, STARTING, RUNNING, STOPPING).",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SwarmStatusCommand implements Callable<Integer> {

    public enum OutputFormat {
        JSON,
        PRETTY
    }

    @SuppressWarnings("FieldMayBeFinal")
    @CommandLine.Option(names = {"--format"},
                        defaultValue = "pretty",
                        description = "The export output format (JSON, PRETTY). Default=PRETTY.")
    private @NotNull OutputFormat format = OutputFormat.PRETTY;

    @CommandLine.Mixin
    private final @NotNull SwarmOptions swarmOptions = new SwarmOptions();

    private final @NotNull Gson gson;
    private final @NotNull RunsApi runsApi;
    private final @NotNull CommanderApi commanderApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;
    private final @NotNull PrintStream out;

    @Inject
    public SwarmStatusCommand(
            final @NotNull Gson gson,
            final @NotNull Provider<RunsApi> runsApi,
            final @NotNull Provider<CommanderApi> commanderApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull PrintStream out) {

        this.gson = gson;
        this.runsApi = runsApi.get();
        this.commanderApi = commanderApi.get();
        this.errorTransformer = errorTransformer;
        this.out = out;
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
        commanderApi.getApiClient().setBasePath(swarmOptions.getCommanderUrl());

        final CommanderStateResponse commanderStatus;
        try {
            commanderStatus = commanderApi.getCommanderStatus();
        } catch (final ApiException apiException) {
            final Error error = errorTransformer.transformError(apiException);
            Logger.error("Could not obtain commander status. {}", error.getDetail());
            System.err.println("Could not obtain commander status. " + error.getDetail());
            return -1;
        }

        if (commanderStatus.getCommanderStatus() != null) {
            final String runId = commanderStatus.getRunId();
            if (runId == null) {
                if (format == OutputFormat.JSON) {
                    final CommanderStatus status = new CommanderStatus(commanderStatus.getCommanderStatus());
                    out.println(gson.toJson(status));
                } else {
                    out.println("Status: " + commanderStatus.getCommanderStatus());
                }
            } else {
                final RunResponse runResponse;
                try {
                    runResponse = runsApi.getRun(runId);
                } catch (final ApiException apiException) {
                    final Error error = errorTransformer.transformError(apiException);
                    Logger.error("Could not obtain run with id '{}'. {}", error.getDetail());
                    System.err.println("Could not obtain run with id '" + runId + "'. " + error.getDetail());
                    return -1;
                }
                if (format == OutputFormat.JSON) {
                    final CommanderStatusWithRun commanderStatusWithRun =
                            new CommanderStatusWithRun(commanderStatus.getCommanderStatus(),
                                    runId,
                                    runResponse.getScenarioId(),
                                    runResponse.getScenarioName(),
                                    runResponse.getScenarioDescription(),
                                    runResponse.getScenarioType(),
                                    runResponse.getRunStatus(),
                                    runResponse.getScenarioStage());
                    out.println(gson.toJson(commanderStatusWithRun));
                } else if (format == OutputFormat.PRETTY) {
                    out.println("Status: " + commanderStatus.getCommanderStatus());
                    out.println("Run-id: " + runId);
                    out.println("Run-Status: " + runResponse.getRunStatus());
                    out.println("Scenario-id: " + runResponse.getScenarioId());
                    out.println("Scenario-name: " + runResponse.getScenarioName());
                    final String scenarioDescription = runResponse.getScenarioDescription();
                    if (scenarioDescription != null && !scenarioDescription.isEmpty()) {
                        out.println("Scenario-description: " + scenarioDescription);
                    }
                    out.println("Scenario-type: " + runResponse.getScenarioType());
                    out.println("Scenario-Stage: " + runResponse.getScenarioStage());
                }
            }
        } else {
            Logger.error("Commander status response did not contain a status.\n", commanderStatus.toString());
            System.err.println("Commander status response did not contain a status.\n " + commanderStatus);
            return -1;
        }
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "SwarmStatusCommand{" +
                "format=" +
                format +
                ", swarmOptions=" +
                swarmOptions +
                ", gson=" +
                gson +
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
