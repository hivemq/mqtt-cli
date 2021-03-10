package com.hivemq.cli.commands.swarm.commander;

import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.CommanderStateResponse;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Yannick Weber
 */
public class SwarmCommanderCLICommand extends AbstractSwarmCommanderCLICommand {

    private final @NotNull ApiClient apiClient;

    @Inject
    public SwarmCommanderCLICommand(final @Named("swarm-cli") @NotNull ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public @NotNull Integer call() throws Exception {
        Logger.trace("Command {}", this);

        // Check if given URL is valid
        final HttpUrl httpUrl = HttpUrl.parse(commanderUrl);
        if (httpUrl == null) {
            Logger.error("URL is not in a valid format: {}", commanderUrl);
            System.err.println("URL is not in a valid format: " + commanderUrl);
            return -1;
        }

        final CommanderApi commanderApi = new CommanderApi(apiClient);
        final CommanderStateResponse commanderStatus = commanderApi.getCommanderStatus();

        if (json) {

        } else {

        }

        return 0;
    }



}
