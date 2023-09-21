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

package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.openapi.hivemq.Schema;
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

    private final @NotNull DataHubSchemasApi schemasApi = mock(DataHubSchemasApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    @Test
    void execute_schemaFound_printSchema() throws ApiException {
        final GetSchemaTask task = new GetSchemaTask(outputFormatter, schemasApi, "test-1", null);

        final Schema schema = new Schema();
        when(schemasApi.getSchema("test-1", null)).thenReturn(schema);

        assertTrue(task.execute());
        verify(schemasApi, times(1)).getSchema("test-1", null);
        verify(outputFormatter).printJson(schema);
    }

    @Test
    void execute_exceptionThrown_printError() throws ApiException {
        final GetSchemaTask task = new GetSchemaTask(outputFormatter, schemasApi, "test-1", null);
        when(schemasApi.getSchema("test-1", null)).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter).printApiException(any(), any());
    }
}
