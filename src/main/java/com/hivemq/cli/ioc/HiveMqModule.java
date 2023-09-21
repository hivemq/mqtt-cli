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
import com.hivemq.cli.commands.hivemq.HiveMQCLICommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyCreateCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyDeleteCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyGetCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyListCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.BehaviorPolicyUpdateCommand;
import com.hivemq.cli.commands.hivemq.behaviorstate.BehaviorStateCommand;
import com.hivemq.cli.commands.hivemq.behaviorstate.BehaviorStateGetCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyCreateCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyDeleteCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyGetCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyListCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyUpdateCommand;
import com.hivemq.cli.commands.hivemq.export.ExportCommand;
import com.hivemq.cli.commands.hivemq.export.clients.ExportClientsCommand;
import com.hivemq.cli.commands.hivemq.schema.SchemaCreateCommand;
import com.hivemq.cli.commands.hivemq.schema.SchemaDeleteCommand;
import com.hivemq.cli.commands.hivemq.schema.SchemaGetCommand;
import com.hivemq.cli.commands.hivemq.schema.SchemaListCommand;
import com.hivemq.cli.commands.hivemq.schema.SchemaCommand;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
class HiveMqModule {

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    @Named("hivemq-cli")
    static @NotNull CommandLine provideHiveMqCli(
            final @NotNull HiveMQCLICommand hivemqCliCommand,
            final @NotNull ExportCommand exportCommand,
            final @NotNull ExportClientsCommand exportClientsCommand,
            final @NotNull DataPolicyCommand dataPolicyCommand,
            final @NotNull DataPolicyGetCommand dataPolicyGetCommand,
            final @NotNull DataPolicyUpdateCommand dataPolicyUpdateCommand,
            final @NotNull DataPolicyListCommand dataPolicyListCommand,
            final @NotNull DataPolicyCreateCommand dataPolicyCreateCommand,
            final @NotNull DataPolicyDeleteCommand dataPolicyDeleteCommand,
            final @NotNull BehaviorPolicyCommand behaviorPolicyCommand,
            final @NotNull BehaviorPolicyGetCommand behaviorPolicyGetCommand,
            final @NotNull BehaviorPolicyUpdateCommand behaviorPolicyUpdateCommand,
            final @NotNull BehaviorPolicyListCommand behaviorPolicyListCommand,
            final @NotNull BehaviorPolicyCreateCommand behaviorPolicyCreateCommand,
            final @NotNull BehaviorPolicyDeleteCommand behaviorPolicyDeleteCommand,
            final @NotNull BehaviorStateCommand behaviorStateCommand,
            final @NotNull BehaviorStateGetCommand behaviorStateGetCommand,
            final @NotNull SchemaCommand schemaCommand,
            final @NotNull SchemaGetCommand schemaGetCommand,
            final @NotNull SchemaListCommand schemaListCommand,
            final @NotNull SchemaCreateCommand schemaCreateCommand,
            final @NotNull SchemaDeleteCommand schemaDeleteCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {

        final CommandLine dataPolicyCommandLine = new CommandLine(dataPolicyCommand).addSubcommand(dataPolicyGetCommand)
                .addSubcommand(dataPolicyUpdateCommand)
                .addSubcommand(dataPolicyListCommand)
                .addSubcommand(dataPolicyCreateCommand)
                .addSubcommand(dataPolicyDeleteCommand);
        final CommandLine behaviorPolicyCommandLine =
                new CommandLine(behaviorPolicyCommand).addSubcommand(behaviorPolicyGetCommand)
                        .addSubcommand(behaviorPolicyUpdateCommand)
                        .addSubcommand(behaviorPolicyListCommand)
                        .addSubcommand(behaviorPolicyCreateCommand)
                        .addSubcommand(behaviorPolicyDeleteCommand);
        final CommandLine behaviorStateCommandLine =
                new CommandLine(behaviorStateCommand).addSubcommand(behaviorStateGetCommand);
        final CommandLine schemaCommandLine = new CommandLine(schemaCommand).addSubcommand(schemaGetCommand)
                .addSubcommand(schemaListCommand)
                .addSubcommand(schemaCreateCommand)
                .addSubcommand(schemaDeleteCommand);

        return new CommandLine(hivemqCliCommand).addSubcommand(new CommandLine(exportCommand).addSubcommand(
                        exportClientsCommand))
                .addSubcommand(dataPolicyCommandLine)
                .addSubcommand(behaviorPolicyCommandLine)
                .addSubcommand(behaviorStateCommandLine)
                .addSubcommand(schemaCommandLine)
                .setColorScheme(config.getColorScheme())
                .setUsageHelpWidth(config.getCliWidth())
                .setParameterExceptionHandler(handler);
    }

}
