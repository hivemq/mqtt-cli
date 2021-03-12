package com.hivemq.cli.commands.swarm.run;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.AbstractSwarmCommand;
import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.ApiResponse;
import com.hivemq.cli.openapi.swarm.*;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "start",
        description = "HiveMQ Swarm Run Start Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class SwarmRunStartCommand extends AbstractSwarmCommand {

    @CommandLine.Option(names = {"-url"}, defaultValue = "http://localhost:8888", description = "The URL of the HiveMQ REST API endpoint (default http://localhost:8888)", order = 1)
    private @NotNull String url;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The scenario file", order = 2)
    private @Nullable File scenario;

    @CommandLine.Option(names = {"-d"}, defaultValue = "false", description = "Log to $HOME/.mqtt.cli/logs (Configurable through $HOME/.mqtt-cli/config.properties)", order = 9)
    private @NotNull Boolean detached;

    private final @NotNull RunsApi runsApi;
    private final @NotNull ScenariosApi scenariosApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;

    @Inject
    public SwarmRunStartCommand(
            final @NotNull RunsApi runsApi,
            final @NotNull ScenariosApi scenariosApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer) {
        this.runsApi = runsApi;
        this.scenariosApi = scenariosApi;
        this.errorTransformer = errorTransformer;
    }

    public SwarmRunStartCommand(
            final @NotNull String url,
            final @Nullable File scenario,
            final @NotNull Boolean detached,
            final @NotNull RunsApi runsApi,
            final @NotNull ScenariosApi scenariosApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer) {

        this.url = url;
        this.scenario = scenario;
        this.detached = detached;
        this.runsApi = runsApi;
        this.scenariosApi = scenariosApi;
        this.errorTransformer = errorTransformer;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        // Check if given URL is valid
        final HttpUrl httpUrl = HttpUrl.parse(commanderUrl);
        if (httpUrl == null) {
            Logger.error("URL is not in a valid format: {}", commanderUrl);
            System.err.println("URL is not in a valid format: " + commanderUrl);
            return -1;
        }

        if (scenario == null) {
            Logger.error("Scenario file is missing. Option '-f' is not set");
            System.err.println("Scenario file is missing. Option '-f' is not set");
            return -1;
        }

        if (scenario.canRead()) {
            Logger.error("File '{}' is not readable.", scenario.getAbsolutePath());
            System.err.println("File '" + scenario.getAbsolutePath() + "' is not readable.");
            return -1;
        }

        if (scenario.exists()) {
            Logger.error("File '{}' does not exist.", scenario.getAbsolutePath());
            System.err.println("File '" + scenario.getAbsolutePath() + "' does not exist.");
            return -1;
        }

        final byte[] bytes;
        try {
            bytes = Files.toByteArray(scenario);
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

        System.out.println("Uploading scenario from file '" + scenario.getAbsolutePath() + "'.");
        final ApiResponse<UploadScenarioResponse> uploadResponse;
        final UploadScenarioResponse uploadScenarioResponse;
        try {
            uploadResponse = scenariosApi.uploadScenarioWithHttpInfo(uploadScenarioRequest);
            uploadScenarioResponse = uploadResponse.getData();
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Could not upload the scenario. {}", error.getDetail());
            System.err.println("Could not upload the scenario. " + error.getDetail());
            return -1;
        }

        final Integer scenarioId = uploadScenarioResponse.getScenarioId();
        if (scenarioId == null) {
            Logger.error("Upload scenario response did not contain a scenario-id:\n {}", uploadScenarioResponse.toString());
            System.err.println("Upload scenario response did not contain a scenario-id:\n " + uploadScenarioResponse.toString());
            return -1;
        }
        System.out.println("Successfully uploaded scenario. Scenario-id: " + scenarioId);

        final StartRunRequest startRunRequest = new StartRunRequest();
        startRunRequest.setScenarioId(scenarioId.toString());

        final ApiResponse<StartRunResponse> startResponse;
        final StartRunResponse startRunResponse;
        try {
            startResponse = runsApi.startRunWithHttpInfo(startRunRequest);
            startRunResponse = startResponse.getData();
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Could not execute the scenario. {}.", error.getDetail());
            System.err.println("Could not execute the scenario. " + error.getDetail());
            return -1;
        }

        final Integer runId = startRunResponse.getRunId();
        if (runId == null) {
            Logger.error("Start run response did not contain a run-id:\n {}", startRunResponse.toString());
            System.err.println("Start run response did not contain a run-id:\n " + startRunResponse.toString());
            return -1;
        }
        System.out.println("Run id: " + runId);
        System.out.println("Run status: " + startRunResponse.getRunStatus());

        if (!detached) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    final StopRunRequest stopRunRequest = new StopRunRequest();
                    stopRunRequest.runStatus("STOPPED");
                    runsApi.stopRun(runId.toString(), stopRunRequest);
                } catch (final ApiException e) {
                    final Error error = errorTransformer.transformError(e);
                    Logger.error("Failed to stop run '{}'. {}.", runId, error.getDetail());
                    System.err.println("Failed to stop run '" + runId + "'. " + error.getDetail());
                }
            }));

            pollUntilFinished(runId);
        }
        return 0;
    }

    private void pollUntilFinished(final int runId) {
        boolean finished = false;
        while (!finished) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            try {
                final RunResponse run = runsApi.getRun(Integer.toString(runId));
                System.out.println("Run status: " + run.getRunStatus());
                System.out.println("Scenario Stage: " + run.getScenarioStage());
                finished = "FINISHED".equals(run.getRunStatus());
            } catch (final ApiException e) {
                final Error error = errorTransformer.transformError(e);
                Logger.error("Failed to obtain run status {}.", error.getDetail());
                System.err.println("Failed to obtain run status " + error.getDetail());
                // we do not exit here, maybe the obtaining of the status works again in the next iteration
                // the user can still exit the command manually
            }
        }
    }

    private @NotNull String getScenarioName(final @NotNull File scenario) {
        final String fileName = scenario.getName();
        if (fileName.endsWith(".vm")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        if (fileName.endsWith(".xml")) {
            return fileName.substring(0, fileName.length() - 4);
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

}
