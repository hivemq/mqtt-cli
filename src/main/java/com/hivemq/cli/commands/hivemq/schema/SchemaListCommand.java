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

package com.hivemq.cli.commands.hivemq.schema;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datahub.DataHubOptions;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.converters.SchemaTypeConverter;
import com.hivemq.cli.hivemq.schemas.ListSchemasTask;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list",
                     description = "List all existing schemas",
                     sortOptions = false,
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SchemaListCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-t", "--type"},
                        converter = SchemaTypeConverter.class,
                        description = "Filter by schema type")
    private @Nullable String @Nullable [] schemaTypes;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--id"}, description = "Filter by schema id")
    private @Nullable String @Nullable [] schemaIds;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-f", "--field"},
                        description = "Filter which JSON fields are included in the response")
    private @Nullable String @Nullable [] fields;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--limit"}, description = "Limit the number of returned schemas")
    private @Nullable Integer limit;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public SchemaListCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final DataHubSchemasApi schemasApi =
                hiveMQRestService.getSchemasApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        if (limit != null && limit < 0) {
            outputFormatter.printError("The limit must not be negative.");
            return 1;
        }

        final ListSchemasTask listSchemasTask =
                new ListSchemasTask(outputFormatter, schemasApi, schemaTypes, schemaIds, fields, limit);

        if (listSchemasTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "ListSchemasCommand{" +
                "schemaTypes=" +
                Arrays.toString(schemaTypes) +
                ", schemaIds=" +
                Arrays.toString(schemaIds) +
                ", fields=" +
                Arrays.toString(fields) +
                ", limit=" +
                limit +
                ", dataGovernanceOptions=" +
                dataHubOptions +
                ", outputFormatter=" +
                outputFormatter +
                ", hiveMQRestService=" +
                hiveMQRestService +
                '}';
    }
}
