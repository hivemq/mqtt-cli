package com.hivemq.cli.ioc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.swarm.SwarmCLICommand;
import com.hivemq.cli.commands.swarm.commander.SwarmStatusCommand;
import com.hivemq.cli.commands.swarm.run.SwarmRunCommand;
import com.hivemq.cli.commands.swarm.run.SwarmRunStartCommand;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.ScenariosApi;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @author Yannick Weber
 */
@Module
public class SwarmCLIModule {

    @Provides
    @Singleton
    @Named("swarm-cli")
    public @NotNull CommandLine provideSwarmCli(
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler,
            final @NotNull SwarmCLICommand swarmCLICommand,
            final @NotNull SwarmStatusCommand swarmStatusCommand,
            final @NotNull SwarmRunStartCommand swarmRunStartCommand,
            final @NotNull SwarmRunCommand swarmRunCommand) {

        return new CommandLine(swarmCLICommand)
                .addSubcommand(swarmStatusCommand)
                .addSubcommand(new CommandLine(swarmRunCommand)
                        .addSubcommand(swarmRunStartCommand))
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }

    @Provides
    @Singleton
    @Named("swarm-cli")
    public @NotNull ApiClient provideApiClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);
        apiClient.setBasePath("/");

        return apiClient;
    }

    @Provides
    @Singleton
    public @NotNull Gson provideGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Provides
    @Singleton
    public @NotNull RunsApi provideRunsApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new RunsApi(apiClient);
    }

    @Provides
    @Singleton
    public @NotNull CommanderApi provideCommanderApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new CommanderApi(apiClient);
    }

    @Provides
    @Singleton
    public @NotNull ScenariosApi provideScenariosApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new ScenariosApi(apiClient);
    }

}
