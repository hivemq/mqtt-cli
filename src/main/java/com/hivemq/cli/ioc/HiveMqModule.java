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
import com.hivemq.cli.commands.hivemq.behaviorpolicy.CreateBehaviorPolicyCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.DeleteBehaviorPolicyCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.GetBehaviorPolicyCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.ListBehaviorPoliciesCommand;
import com.hivemq.cli.commands.hivemq.behaviorpolicy.UpdateBehaviorPolicyCommand;
import com.hivemq.cli.commands.hivemq.behaviorstate.BehaviorStateCommand;
import com.hivemq.cli.commands.hivemq.behaviorstate.GetBehaviorStateCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.CreateDataPolicyCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DataPolicyCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.DeleteDataPolicyCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.GetDataPolicyCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.ListDataPoliciesCommand;
import com.hivemq.cli.commands.hivemq.datapolicy.UpdateDataPolicyCommand;
import com.hivemq.cli.commands.hivemq.export.ExportCommand;
import com.hivemq.cli.commands.hivemq.export.clients.ExportClientsCommand;
import com.hivemq.cli.commands.hivemq.schema.CreateSchemaCommand;
import com.hivemq.cli.commands.hivemq.schema.DeleteSchemaCommand;
import com.hivemq.cli.commands.hivemq.schema.GetSchemaCommand;
import com.hivemq.cli.commands.hivemq.schema.ListSchemaCommand;
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
            final @NotNull GetDataPolicyCommand getDataPolicyCommand,
            final @NotNull UpdateDataPolicyCommand updateDataPolicyCommand,
            final @NotNull ListDataPoliciesCommand listDataPoliciesCommand,
            final @NotNull CreateDataPolicyCommand createDataPolicyCommand,
            final @NotNull DeleteDataPolicyCommand deleteDataPolicyCommand,
            final @NotNull BehaviorPolicyCommand behaviorPolicyCommand,
            final @NotNull GetBehaviorPolicyCommand getBehaviorPolicyCommand,
            final @NotNull UpdateBehaviorPolicyCommand updateBehaviorPolicyCommand,
            final @NotNull ListBehaviorPoliciesCommand listBehaviorPoliciesCommand,
            final @NotNull CreateBehaviorPolicyCommand createBehaviorPolicyCommand,
            final @NotNull DeleteBehaviorPolicyCommand deleteBehaviorPolicyCommand,
            final @NotNull BehaviorStateCommand behaviorStateCommand,
            final @NotNull GetBehaviorStateCommand getBehaviorStateCommand,
            final @NotNull SchemaCommand schemaCommand,
            final @NotNull GetSchemaCommand getSchemaCommand,
            final @NotNull ListSchemaCommand listSchemaCommand,
            final @NotNull CreateSchemaCommand createSchemaCommand,
            final @NotNull DeleteSchemaCommand deleteSchemaCommand,
            final @NotNull CommandLineConfig config,
            final @NotNull CommandErrorMessageHandler handler) {

        final CommandLine dataPolicyCommandLine = new CommandLine(dataPolicyCommand).addSubcommand(getDataPolicyCommand)
                .addSubcommand(updateDataPolicyCommand)
                .addSubcommand(listDataPoliciesCommand)
                .addSubcommand(createDataPolicyCommand)
                .addSubcommand(deleteDataPolicyCommand);
        final CommandLine behaviorPolicyCommandLine =
                new CommandLine(behaviorPolicyCommand).addSubcommand(getBehaviorPolicyCommand)
                        .addSubcommand(updateBehaviorPolicyCommand)
                        .addSubcommand(listBehaviorPoliciesCommand)
                        .addSubcommand(createBehaviorPolicyCommand)
                        .addSubcommand(deleteBehaviorPolicyCommand);
        final CommandLine behaviorStateCommandLine =
                new CommandLine(behaviorStateCommand).addSubcommand(getBehaviorStateCommand);
        final CommandLine schemaCommandLine = new CommandLine(schemaCommand).addSubcommand(getSchemaCommand)
                .addSubcommand(listSchemaCommand)
                .addSubcommand(createSchemaCommand)
                .addSubcommand(deleteSchemaCommand);

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
