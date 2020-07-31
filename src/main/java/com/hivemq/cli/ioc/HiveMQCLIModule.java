/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.ioc;

import com.hivemq.cli.commandline.CommandErrorMessageHandler;
import com.hivemq.cli.commandline.CommandLineConfig;
import com.hivemq.cli.commands.hivemq.HiveMQCLICommand;
import com.hivemq.cli.commands.hivemq.export.ExportCommand;
import com.hivemq.cli.commands.hivemq.export.clients.ExportClientsCommand;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class HiveMQCLIModule {

    @Provides
    @Singleton
    @Named("hivemq-cli")
    static @NotNull CommandLine provideHiveMqCli(
            final @NotNull HiveMQCLICommand hivemqCliCommand,
            final @NotNull ExportCommand exportCommand,
            final @NotNull ExportClientsCommand exportClientsCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {

        return new CommandLine(hivemqCliCommand)
                .addSubcommand(new CommandLine(exportCommand)
                        .addSubcommand(exportClientsCommand))
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }
}
