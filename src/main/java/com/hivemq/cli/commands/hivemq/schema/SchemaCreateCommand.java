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
import com.hivemq.cli.commands.hivemq.datahub.SchemaDefinitionOptions;
import com.hivemq.cli.converters.SchemaTypeConverter;
import com.hivemq.cli.hivemq.schemas.CreateSchemaTask;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
                     description = "Create a new schema",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SchemaCreateCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The id of the schema")
    private @NotNull String schemaId;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"--type"},
                        required = true,
                        converter = SchemaTypeConverter.class,
                        description = "The schema type (default json)")
    private @NotNull String schemaType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--message-type"},
                        description = "The Protobuf message type. Only used with --type PROTOBUF.")
    private @Nullable String messageType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--allow-unknown"},
                        defaultValue = "false",
                        description = "Allow unknown Protobuf fields. Only used with --type PROTOBUF.")
    private boolean allowUnknown;


    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--print-version"},
                        defaultValue = "false",
                        description = "Print the assigned schema version after successful creation.")
    private boolean printVersion;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull SchemaDefinitionOptions definitionOptions;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public SchemaCreateCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() throws IOException {
        Logger.trace("Command {}", this);

        final DataHubSchemasApi schemasApi =
                hiveMQRestService.getSchemasApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        if (schemaType.equals("PROTOBUF") && messageType == null) {
            outputFormatter.printError("Protobuf message type is missing. Option '--message-type' is not set.");
            return 1;
        }

        if (schemaType.equals("JSON") && messageType != null) {
            outputFormatter.printError("Option '--message-type' is not applicable to schemas of type 'JSON'.");
            return 1;
        }

        final String fileDefinition = definitionOptions.getFile();
        final ByteBuffer argumentDefinition = definitionOptions.getArgument();
        final ByteBuffer definitionBytes;
        if (fileDefinition != null) {
            try {
                final Path path = Paths.get(fileDefinition);
                definitionBytes = ByteBuffer.wrap(Files.readAllBytes(path));
            } catch (final IOException exception) {
                outputFormatter.printError("File not found or not readable: " + fileDefinition);
                return 1;
            }
        } else if (argumentDefinition != null) {
            definitionBytes = argumentDefinition;
        } else {
            throw new RuntimeException("One of --file or --definition must be set.");
        }

        final CreateSchemaTask createSchemaTask = new CreateSchemaTask(outputFormatter,
                schemasApi,
                schemaId,
                schemaType,
                messageType,
                allowUnknown,
                printVersion,
                definitionBytes);
        if (createSchemaTask.execute()) {
            return 0;
        } else {
            if (schemaType.equals("PROTOBUF") && fileDefinition != null) {
                final String fileExtension = FilenameUtils.getExtension(fileDefinition);
                if (fileExtension.equalsIgnoreCase("proto")) {
                    outputFormatter.printError(
                            "Hint: the provided definition file must be a compiled descriptor file (.desc), not a .proto file.");
                }
            }

            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "CreateSchemaCommand{" +
                "schemaId='" +
                schemaId +
                '\'' +
                ", schemaType='" +
                schemaType +
                '\'' +
                ", messageType='" +
                messageType +
                '\'' +
                ", allowUnknown=" +
                allowUnknown +
                ", definitionOptions=" +
                definitionOptions +
                ", dataGovernanceOptions=" +
                dataHubOptions +
                ", outputFormatter=" +
                outputFormatter +
                '}';
    }
}
