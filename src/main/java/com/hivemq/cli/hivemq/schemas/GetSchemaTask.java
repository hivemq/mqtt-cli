package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;

public class GetSchemaTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull SchemasApi schemasApi;
    private final @NotNull String schemaId;

    public GetSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull SchemasApi schemasApi,
            final @NotNull String schemaId) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
    }

    public boolean execute() {
        final Schema schema;
        try {
            schema = schemasApi.getSchema(schemaId, null);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to get schema", apiException);
            return false;
        }

        outputFormatter.printJson(schema);

        return true;
    }
}
