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

package com.hivemq.cli.commands.hivemq.schemas;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.schemas.GetSchemaTask;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "get",
                     description = "Get an existing schema",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class GetSchemaCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The id of the schema")
    private @NotNull String schemaId;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-f", "--field"},
                        description = "Filter which JSON fields are included in the response")
    private @Nullable String @Nullable [] fields;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public GetSchemaCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final DataHubSchemasApi schemasApi =
                hiveMQRestService.getSchemasApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        if (schemaId.isEmpty()) {
            outputFormatter.printError("The schema id must not be empty.");
            return 1;
        }

        final GetSchemaTask getSchemaTask = new GetSchemaTask(outputFormatter, schemasApi, schemaId, fields);
        if (getSchemaTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "GetSchemaCommand{" +
                "schemaId='" +
                schemaId +
                '\'' +
                ", fields=" +
                Arrays.toString(fields) +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", outputFormatter=" +
                outputFormatter +
                ", hiveMQRestService=" +
                hiveMQRestService +
                '}';
    }
}
