package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;

public class DeleteSchemaTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull SchemasApi schemasApi;
    private final @NotNull String schemaId;

    public DeleteSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull SchemasApi schemasApi,
            final @NotNull String schemaId) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
    }

    public boolean execute() {
        try {
            schemasApi.deleteSchema(schemaId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to delete schema", apiException);
            return false;
        }

        return true;
    }
}
