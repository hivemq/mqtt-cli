package com.hivemq.cli.ioc;

import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.hivemq.HiveMQCLICommand;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.Configuration;
import dagger.Provides;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

public class SwarmCLIModule {

    @Provides
    @Singleton
    @Named("swarm-cli")
    public @NotNull CommandLine provideSwarmCli(
            final @NotNull HiveMQCLICommand hivemqCliCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {

        return new CommandLine(hivemqCliCommand)

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

}
