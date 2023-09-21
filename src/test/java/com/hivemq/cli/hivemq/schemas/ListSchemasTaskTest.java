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
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemaList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListSchemasTaskTest {

    private final @NotNull DataHubSchemasApi schemasApi = mock(DataHubSchemasApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    @Test
    void execute_schemaIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] schemaIds = {"schema-1", "schema-2", "schema-3"};
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, null, schemaIds, null, null);

        when(schemasApi.getAllSchemas(any(), any(), any(), any(), any())).thenReturn(new SchemaList());

        assertTrue(task.execute());
        final String schemaIdsQueryParam = "schema-1,schema-2,schema-3";
        verify(schemasApi, times(1)).getAllSchemas(isNull(), isNull(), eq(schemaIdsQueryParam), any(), isNull());
    }

    @Test
    void execute_schemaTypesProvided_usedAsUrlParameter() throws ApiException {
        final String[] schemaTypes = {"type-a", "type-b", "type-c"};
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, schemaTypes, null, null, null);

        when(schemasApi.getAllSchemas(any(), any(), any(), any(), any())).thenReturn(new SchemaList());

        assertTrue(task.execute());
        final String schemaTypesQueryParam = "type-a,type-b,type-c";
        verify(schemasApi, times(1)).getAllSchemas(isNull(), eq(schemaTypesQueryParam), isNull(), any(), isNull());
    }

    @Test
    void execute_fieldsProvided_usedAsUrlParameter() throws ApiException {
        final String[] fields = {"id", "version", "createdAt"};
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, null, null, fields, null);

        when(schemasApi.getAllSchemas(any(), any(), any(), any(), any())).thenReturn(new SchemaList());

        assertTrue(task.execute());
        final String fieldsQueryParam = "id,version,createdAt";
        verify(schemasApi, times(1)).getAllSchemas(eq(fieldsQueryParam), isNull(), isNull(), any(), isNull());
    }

    @Test
    void execute_cursorReturned_allPagesFetched() throws ApiException {
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, null, null, null, null);

        final Schema schema1 = new Schema().id("schema-1");
        final Schema schema2 = new Schema().id("schema-2");
        final Schema schema3 = new Schema().id("schema-3");
        final Schema schema4 = new Schema().id("schema-4");

        final String cursorPrefix = "/api/v1/data-validation/schemas?cursor=";
        final SchemaList page1 = new SchemaList().items(Collections.singletonList(schema1))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final SchemaList page2 = new SchemaList().items(Arrays.asList(schema2, schema3))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        final SchemaList page3 = new SchemaList().items(Collections.singletonList(schema4));
        when(schemasApi.getAllSchemas(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(schemasApi.getAllSchemas(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);
        when(schemasApi.getAllSchemas(any(), any(), any(), any(), eq("cursor-2"))).thenReturn(page3);

        assertTrue(task.execute());

        verify(schemasApi).getAllSchemas(isNull(), isNull(), isNull(), any(), isNull());
        verify(schemasApi).getAllSchemas(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(schemasApi).getAllSchemas(isNull(), isNull(), isNull(), any(), eq("cursor-2"));
        verify(schemasApi, times(3)).getAllSchemas(any(), any(), any(), any(), any());

        final ArgumentCaptor<SchemaList> outputCaptor = ArgumentCaptor.forClass(SchemaList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(schema1, schema2, schema3, schema4), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_cursorReturnedLimitSpecified_limitNotExceeded() throws ApiException {
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, null, null, null, 3);

        final Schema schema1 = new Schema().id("schema-1");
        final Schema schema2 = new Schema().id("schema-2");
        final Schema schema3 = new Schema().id("schema-3");
        final Schema schema4 = new Schema().id("schema-4");

        final String cursorPrefix = "/api/v1/data-validation/schemas?cursor=";
        final SchemaList page1 = new SchemaList().items(Arrays.asList(schema1, schema2))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final SchemaList page2 = new SchemaList().items(Arrays.asList(schema3, schema4))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        when(schemasApi.getAllSchemas(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(schemasApi.getAllSchemas(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);

        assertTrue(task.execute());

        verify(schemasApi).getAllSchemas(isNull(), isNull(), isNull(), any(), isNull());
        verify(schemasApi).getAllSchemas(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(schemasApi, times(2)).getAllSchemas(any(), any(), any(), any(), any());

        final ArgumentCaptor<SchemaList> outputCaptor = ArgumentCaptor.forClass(SchemaList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(schema1, schema2, schema3), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final ListSchemasTask task = new ListSchemasTask(outputFormatter, schemasApi, null, null, null, null);

        when(schemasApi.getAllSchemas(any(), any(), any(), any(), any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
