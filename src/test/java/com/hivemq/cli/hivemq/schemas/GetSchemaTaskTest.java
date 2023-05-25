package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetSchemaTaskTest {

    private final @NotNull SchemasApi schemasApi = mock(SchemasApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    @Test
    void execute_schemaFound_printSchema() throws ApiException {
        final GetSchemaTask task = new GetSchemaTask(outputFormatter, schemasApi, "test-1");

        final Schema schema = new Schema();
        when(schemasApi.getSchema("test-1", null)).thenReturn(schema);

        assertTrue(task.execute());
        verify(schemasApi, times(1)).getSchema("test-1", null);
        verify(outputFormatter).printJson(schema);
    }

    @Test
    void execute_exceptionThrown_printError() throws ApiException {
        final GetSchemaTask task = new GetSchemaTask(outputFormatter, schemasApi, "test-1");
        when(schemasApi.getSchema("test-1", null)).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter).printApiException(any(), any());
    }
}
