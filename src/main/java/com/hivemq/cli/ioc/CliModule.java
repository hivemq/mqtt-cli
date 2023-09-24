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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.hivemq.cli.DefaultCLIProperties;
import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.MqttCLICommand;
import com.hivemq.cli.commands.cli.PublishCommand;
import com.hivemq.cli.commands.cli.SubscribeCommand;
import com.hivemq.cli.commands.cli.TestBrokerCommand;
import com.hivemq.cli.commands.shell.ShellCommand;
import com.hivemq.cli.openapi.hivemq.BehaviorPolicy;
import com.hivemq.cli.openapi.hivemq.DataPolicy;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.utils.json.BehaviorPolicySerializer;
import com.hivemq.cli.utils.json.OffsetDateTimeSerializer;
import com.hivemq.cli.utils.json.DataPolicySerializer;
import com.hivemq.cli.utils.json.SchemaSerializer;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

@Module
class CliModule {

    private static final @NotNull Path PROPERTIES_FILE_PATH =
            Paths.get(System.getProperty("user.home")).resolve(".mqtt-cli").resolve("config.properties");

    @Provides
    @Singleton
    @Named("cli")
    static @NotNull CommandLine provideCli(
            final @NotNull MqttCLICommand main,
            final @NotNull PublishCommand publishCommand,
            final @NotNull SubscribeCommand subscribeCommand,
            final @NotNull ShellCommand shellCommand,
            final @NotNull TestBrokerCommand testBrokerCommand,
            final @NotNull @Named("hivemq-cli") CommandLine hivemqCliCommandLine,
            final @NotNull @Named("swarm-cli") CommandLine swarmCLICommand,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {
        return new CommandLine(main).addSubcommand(publishCommand)
                .addSubcommand(subscribeCommand)
                .addSubcommand(shellCommand)
                .addSubcommand(testBrokerCommand)
                .addSubcommand(hivemqCliCommandLine)
                .addSubcommand(swarmCLICommand)
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler)
                .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @Provides
    @Singleton
    static @NotNull DefaultCLIProperties provideDefaultProperties() {
        return new DefaultCLIProperties(PROPERTIES_FILE_PATH);
    }

    @Provides
    @NotNull Gson provideGson() {
        return new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
                .registerTypeAdapter(DataPolicy.class, new DataPolicySerializer())
                .registerTypeAdapter(BehaviorPolicy.class, new BehaviorPolicySerializer())
                .registerTypeAdapter(Schema.class, new SchemaSerializer())
                .create();
    }

    @Provides
    public @NotNull PrintStream provideOutStream() {
        return System.out;
    }
}
