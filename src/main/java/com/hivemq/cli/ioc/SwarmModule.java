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

package com.hivemq.cli.ioc;

import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.swarm.SwarmCLICommand;
import com.hivemq.cli.commands.swarm.commander.SwarmStatusCommand;
import com.hivemq.cli.commands.swarm.run.SwarmRunCommand;
import com.hivemq.cli.commands.swarm.run.SwarmRunStartCommand;
import com.hivemq.cli.commands.swarm.run.SwarmRunStopCommand;
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

@Module
class SwarmModule {

    @Provides
    @Singleton
    @Named("swarm-cli")
    public @NotNull CommandLine provideSwarmCli(
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler,
            final @NotNull SwarmCLICommand swarmCLICommand,
            final @NotNull SwarmStatusCommand swarmStatusCommand,
            final @NotNull SwarmRunStartCommand swarmRunStartCommand,
            final @NotNull SwarmRunStopCommand swarmRunStopCommand,
            final @NotNull SwarmRunCommand swarmRunCommand) {
        return new CommandLine(swarmCLICommand) //
                .addSubcommand(swarmStatusCommand)
                .addSubcommand(new CommandLine(swarmRunCommand).addSubcommand(swarmRunStartCommand)
                        .addSubcommand(swarmRunStopCommand))
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }

    @Provides
    @Named("swarm-cli")
    public @NotNull ApiClient provideApiClient() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();
        final ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setHttpClient(okHttpClient);

        return apiClient;
    }

    @Provides
    public @NotNull RunsApi provideRunsApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new RunsApi(apiClient);
    }

    @Provides
    public @NotNull CommanderApi provideCommanderApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new CommanderApi(apiClient);
    }

    @Provides
    public @NotNull ScenariosApi provideScenariosApi(final @NotNull @Named("swarm-cli") ApiClient apiClient) {
        return new ScenariosApi(apiClient);
    }
}
