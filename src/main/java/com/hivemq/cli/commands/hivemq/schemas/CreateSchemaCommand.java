package com.hivemq.cli.commands.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.commands.hivemq.datagovernance.SchemaDefinitionOptions;
import com.hivemq.cli.converters.SchemaTypeConverter;
import com.hivemq.cli.hivemq.schemas.CreateSchemaTask;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
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

@CommandLine.Command(name = "create", description = "Create a new schema", mixinStandardHelpOptions = true)
public class CreateSchemaCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "the id of the schema")
    private @NotNull String schemaId;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"--type"},
                        required = true,
                        converter = SchemaTypeConverter.class,
                        description = "the schema type (default json) ")
    private @NotNull String schemaType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--message-type"},
                        description = "the Protobuf message type. Only used with --type protobuf.")
    private @Nullable String messageType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--allow-unknown"},
                        defaultValue = "false",
                        description = "allow unknown Protobuf fields (default false). Only used with --type protobuf.")
    private boolean allowUnknown;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull SchemaDefinitionOptions definitionOptions;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;

    @Inject
    public CreateSchemaCommand(final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
    }

    @Override
    public @NotNull Integer call() throws IOException {
        Logger.trace("Command {}", this);

        final SchemasApi schemasApi =
                HiveMQRestService.getSchemasApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        if (schemaType.equals("protobuf") && messageType == null) {
            outputFormatter.printError("Protobuf message type is missing. Option '--message-type' is not set.");
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
                definitionBytes);
        if (createSchemaTask.execute()) {
            return 0;
        } else {
            if (schemaType.equals("protobuf") && fileDefinition != null) {
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
                dataGovernanceOptions +
                ", outputFormatter=" +
                outputFormatter +
                '}';
    }
}
