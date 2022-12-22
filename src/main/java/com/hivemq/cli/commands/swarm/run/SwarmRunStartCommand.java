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
import com.hivemq.cli.openapi.swarm.RunResponse;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.ScenariosApi;
import com.hivemq.cli.openapi.swarm.StartRunRequest;
import com.hivemq.cli.openapi.swarm.StartRunResponse;
import com.hivemq.cli.openapi.swarm.UploadScenarioRequest;
import com.hivemq.cli.openapi.swarm.UploadScenarioResponse;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "start",
                     description = "Start HiveMQ Swarm runs.",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SwarmRunStartCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-f", "--file"},
                        description = "The scenario file. " +
                                "If a scenario file is given this command uploads, executes and deletes the scenario afterwards.",
                        required = true)
    private @Nullable File scenario;

    @CommandLine.Option(names = {"-d", "--detach"},
                        defaultValue = "false",
                        description = "Run the command in detached mode. " +
                                "In detached mode the command uploads and executes the scenario and does not wait until the scenario is finished. " +
                                "The scenario is not deleted afterwards.")
    private @NotNull Boolean detached = false;

    @CommandLine.Mixin
    private @NotNull SwarmOptions swarmOptions = new SwarmOptions();

    private final @NotNull RunsApi runsApi;
    private final @NotNull ScenariosApi scenariosApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;
    private final @NotNull PrintStream out;

    @Inject
    public SwarmRunStartCommand(
            final @NotNull Provider<RunsApi> runsApi,
            final @NotNull Provider<ScenariosApi> scenariosApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull PrintStream out) {
        this.runsApi = runsApi.get();
        this.scenariosApi = scenariosApi.get();
        this.errorTransformer = errorTransformer;
        this.out = out;
    }

    @VisibleForTesting
    SwarmRunStartCommand(
            final @NotNull String commanderUrl,
            final @Nullable File scenario,
            final @NotNull Boolean detached,
            final @NotNull RunsApi runsApi,
            final @NotNull ScenariosApi scenariosApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull PrintStream out) {
        this(() -> runsApi, () -> scenariosApi, errorTransformer, out);
        this.swarmOptions = new SwarmOptions(commanderUrl);
        this.scenario = scenario;
        this.detached = detached;
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
        scenariosApi.getApiClient().setBasePath(swarmOptions.getCommanderUrl());

        if (scenario == null) {
            Logger.error("Scenario file is missing. Option '-f' is not set");
            System.err.println("Scenario file is missing. Option '-f' is not set");
            return -1;
        }

        if (!scenario.exists()) {
            Logger.error("File '{}' does not exist.", scenario.getAbsolutePath());
            System.err.println("File '" + scenario.getAbsolutePath() + "' does not exist.");
            return -1;
        }

        if (!scenario.canRead()) {
            Logger.error("File '{}' is not readable.", scenario.getAbsolutePath());
            System.err.println("File '" + scenario.getAbsolutePath() + "' is not readable.");
            return -1;
        }

        final byte[] bytes;
        try {
            bytes = Files.readAllBytes(scenario.toPath());
        } catch (final IOException e) {
            Logger.error("Could not read '{}'.", scenario.getAbsolutePath());
            System.err.println("Could not read '" + scenario.getAbsolutePath() + "'.");
            return -1;
        }
        final String scenarioBase64 = Base64.getEncoder().encodeToString(bytes);

        final UploadScenarioRequest uploadScenarioRequest = new UploadScenarioRequest();
        uploadScenarioRequest.setScenario(scenarioBase64);

        try {
            uploadScenarioRequest.scenarioName(getScenarioName(scenario));
            uploadScenarioRequest.scenarioType(getScenarioType(scenario));
        } catch (final IllegalArgumentException e) {
            Logger.error("File '{}' does not end with '.xml' or '.vm'.", scenario.getAbsolutePath());
            System.err.println("File '" + scenario.getAbsolutePath() + "' not end with '.xml' or '.vm'.");
            return -1;
        }

        out.println("Uploading scenario from file '" + scenario.getAbsolutePath() + "'.");
        final UploadScenarioResponse uploadResponse;
        try {
            uploadResponse = scenariosApi.uploadScenario(uploadScenarioRequest);
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Could not upload the scenario. {}", error.getDetail());
            System.err.println("Could not upload the scenario. " + error.getDetail());
            return -1;
        }

        final Integer scenarioId = uploadResponse.getScenarioId();
        if (scenarioId == null) {
            Logger.error("Upload scenario response did not contain a scenario-id:\n {}", uploadResponse.toString());
            System.err.println("Upload scenario response did not contain a scenario-id:\n " + uploadResponse);
            return -1;
        }
        out.println("Successfully uploaded scenario. Scenario-id: " + scenarioId);

        final StartRunRequest startRunRequest = new StartRunRequest();
        startRunRequest.setScenarioId(scenarioId.toString());

        final StartRunResponse startRunResponse;
        try {
            startRunResponse = runsApi.startRun(startRunRequest);
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Could not execute the scenario. {}.", error.getDetail());
            System.err.println("Could not execute the scenario. " + error.getDetail());
            return -1;
        }

        final Integer runId = startRunResponse.getRunId();
        if (runId == null) {
            Logger.error("Start run response did not contain a run-id:\n {}", startRunResponse.toString());
            System.err.println("Start run response did not contain a run-id:\n " + startRunResponse);
            return -1;
        }
        out.println("Run id: " + runId);
        out.println("Run status: " + startRunResponse.getRunStatus());

        if (!detached) {
            Runtime.getRuntime()
                    .addShutdownHook(new SwarmRunStartCommandShutdownHook(runsApi,
                            scenariosApi,
                            errorTransformer,
                            runId,
                            scenarioId));
            try {
                pollUntilFinished(runId);
            } catch (final ApiException apiException) {
                final Error error = errorTransformer.transformError(apiException);
                Logger.error("Failed to obtain run status {}.", error.getDetail());
                System.err.println("Failed to obtain run status " + error.getDetail());
                apiException.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    private void pollUntilFinished(final int runId) throws ApiException {
        boolean finished = false;
        while (!finished) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            final RunResponse run = runsApi.getRun(Integer.toString(runId));
            out.println("Run status: " + run.getRunStatus());
            out.println("Scenario Stage: " + run.getScenarioStage());
            finished = isTerminated(run);
        }
    }

    private boolean isTerminated(final @NotNull RunResponse run) {
        return "FINISHED".equals(run.getRunStatus()) ||
                "STOPPED".equals(run.getRunStatus()) ||
                "FAILED".equals(run.getRunStatus());
    }

    private @NotNull String getScenarioName(final @NotNull File scenario) {
        final String fileName = scenario.getName();
        if (fileName.endsWith(".vm")) {
            return UUID.randomUUID() + "-" + fileName.substring(0, fileName.length() - 3);
        }
        if (fileName.endsWith(".xml")) {
            return UUID.randomUUID() + "-" + fileName.substring(0, fileName.length() - 4);
        }
        throw new IllegalArgumentException("Invalid scenario file ending.");
    }

    private @NotNull String getScenarioType(final @NotNull File scenario) {
        final String fileName = scenario.getName();
        if (fileName.endsWith(".vm")) {
            return "VM";
        }
        if (fileName.endsWith(".xml")) {
            return "XML";
        }
        throw new IllegalArgumentException("Invalid scenario file ending.");
    }

    @Override
    public @NotNull String toString() {
        return "SwarmRunStartCommand{" +
                "scenario=" +
                scenario +
                ", detached=" +
                detached +
                ", swarmOptions=" +
                swarmOptions +
                ", runsApi=" +
                runsApi +
                ", scenariosApi=" +
                scenariosApi +
                ", errorTransformer=" +
                errorTransformer +
                ", out=" +
                out +
                '}';
    }
}
