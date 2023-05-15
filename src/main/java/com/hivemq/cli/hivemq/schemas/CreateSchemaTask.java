package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateSchemaTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull SchemasApi schemasApi;
    private final @NotNull String schemaId;
    private final @NotNull String schemaType;
    private final @Nullable String messageType;
    private final boolean allowUnknown;
    private final @NotNull ByteBuffer definition;

    public CreateSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull SchemasApi schemasApi,
            final @NotNull String schemaId,
            final @NotNull String schemaType,
            final @Nullable String messageType,
            final boolean allowUnknown,
            final @NotNull ByteBuffer definition) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
        this.schemaType = schemaType;
        this.messageType = messageType;
        this.allowUnknown = allowUnknown;
        this.definition = definition;
    }

    public boolean execute() {
        Map<String, String> arguments = null;
        if (schemaType.equals("protobuf")) {
            arguments = new HashMap<>();

            arguments.put("messageType", Objects.requireNonNull(messageType));
            arguments.put("allowUnknownFields", String.valueOf(allowUnknown));
        }

        final String definitionBase64 = Base64.getEncoder().encodeToString(definition.array());

        final Schema schema =
                new Schema().id(schemaId).type(schemaType).schemaDefinition(definitionBase64).arguments(arguments);

        try {
            schemasApi.createSchema(schema);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to create schema", apiException);
            return false;
        }

        return true;
    }
}
