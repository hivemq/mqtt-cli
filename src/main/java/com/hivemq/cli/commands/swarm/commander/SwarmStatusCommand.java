package com.hivemq.cli.commands.swarm.commander;

import com.google.gson.Gson;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.AbstractSwarmCommand;
import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
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

/**
 * @author Yannick Weber
 */
/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "commander",
        description = "HiveMQ Swarm Commander Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class SwarmStatusCommand extends AbstractSwarmCommand {

    private final @NotNull Gson gson;
    private final @NotNull RunsApi runsApi;
    private final @NotNull CommanderApi commanderApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;

    @Inject
    public SwarmStatusCommand(
            final @NotNull Gson gson,
            final @NotNull RunsApi runsApi,
            final @NotNull CommanderApi commanderApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer) {

        this.gson = gson;
        this.runsApi = runsApi;
        this.commanderApi = commanderApi;
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
                    System.out.println(gson.toJson(status));
                } else {
                    System.out.println("Status:" + commanderStatus.getCommanderStatus());
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
                    final CommanderStatusWithRun commanderStatusWithRun = new CommanderStatusWithRun(
                            commanderStatus.getCommanderStatus(),
                            runId,
                            runResponse.getScenarioId(),
                            runResponse.getScenarioName(),
                            runResponse.getScenarioDescription(),
                            runResponse.getScenarioType(),
                            runResponse.getRunStatus(),
                            runResponse.getScenarioStage()
                    );
                    System.out.println(gson.toJson(commanderStatusWithRun));
                } else if (format == OutputFormat.PRETTY) {
                    System.out.println("Status:" + commanderStatus.getCommanderStatus());
                    System.out.println("Run-id:" + runId);
                    System.out.println("Run-Status: " + runResponse.getRunStatus());
                    System.out.println("Scenario-id:" + runResponse.getScenarioId());
                    System.out.println("Scenario-name:" + runResponse.getScenarioName());
                    System.out.println("Scenario-description:" + runResponse.getScenarioDescription());
                    System.out.println("Scenario-type:" + runResponse.getScenarioType());
                    System.out.println("Scenario-Stage: " + runResponse.getScenarioStage());
                }
            }

        } else {
            Logger.error("Commander status response did not contain a status.\n", commanderStatus.toString());
            System.err.println("Commander status response did not contain a status.\n " + commanderStatus.toString());
            return -1;
        }
        return 0;
    }

}
