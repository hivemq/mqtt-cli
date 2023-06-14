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

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateSchemaTaskTest {

    private final @NotNull SchemasApi schemasApi = mock(SchemasApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final @NotNull JSON openapiSerialization = new JSON();

    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String JSON_SCHEMA_DEFINITION = "{ \"type\": \"object\" }";

    // test.proto:
    // ```
    // syntax = "proto3";
    // message Test {}
    // ```
    // Created with `protoc test.proto -o /dev/stdout | base64`
    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String PROTOBUF_SCHEMA_DEFINITION = "ChwKCnRlc3QucHJvdG8iBgoEVGVzdGIGcHJvdG8z";

    private final @NotNull String API_RESPONSE_SCHEMA_JSON = "{ \"version\": 1, \"id\": \"test\" }";

    @Test
    void execute_validJsonSchema_created() throws ApiException {
        final CreateSchemaTask task = new CreateSchemaTask(outputFormatter,
                schemasApi,
                "test-1",
                "JSON",
                null,
                false,
                false,
                ByteBuffer.wrap(JSON_SCHEMA_DEFINITION.getBytes(StandardCharsets.UTF_8)));

        final ArgumentCaptor<Schema> schemaCaptor = ArgumentCaptor.forClass(Schema.class);

        assertTrue(task.execute());

        verify(schemasApi, times(1)).createSchema(schemaCaptor.capture());
        final Schema createdSchema = schemaCaptor.getValue();
        assertEquals("test-1", createdSchema.getId());
        assertEquals("JSON", createdSchema.getType());
        final String createdSchemaDefinition = new String(Base64.decode(createdSchema.getSchemaDefinition()));
        assertEquals(JSON_SCHEMA_DEFINITION, createdSchemaDefinition);
        assertNull(createdSchema.getArguments());
    }

    @Test
    void execute_validProtobufSchema_created() throws ApiException {
        final CreateSchemaTask task = new CreateSchemaTask(outputFormatter,
                schemasApi,
                "test-1",
                "PROTOBUF",
                "Test",
                true,
                false,
                ByteBuffer.wrap(Base64.decode(PROTOBUF_SCHEMA_DEFINITION)));

        final ArgumentCaptor<Schema> schemaCaptor = ArgumentCaptor.forClass(Schema.class);

        assertTrue(task.execute());
        verify(schemasApi, times(1)).createSchema(schemaCaptor.capture());
        final Schema createdSchema = schemaCaptor.getValue();
        assertEquals("test-1", createdSchema.getId());
        assertEquals("PROTOBUF", createdSchema.getType());
        assertEquals(PROTOBUF_SCHEMA_DEFINITION, createdSchema.getSchemaDefinition());
        assertNotNull(createdSchema.getArguments());
        assertEquals("Test", createdSchema.getArguments().get("messageType"));
        assertEquals("true", createdSchema.getArguments().get("allowUnknownFields"));
    }

    @Test
    void execute_printEntireSchemaNotSet_printVersionOnly() throws ApiException {
        final Schema schema = openapiSerialization.deserialize(API_RESPONSE_SCHEMA_JSON, Schema.class);
        when(schemasApi.createSchema(any())).thenReturn(schema);

        final CreateSchemaTask task = new CreateSchemaTask(outputFormatter,
                schemasApi,
                "test-1",
                "JSON",
                null,
                false,
                false,
                ByteBuffer.wrap(new byte[]{}));

        assertTrue(task.execute());
        verify(outputFormatter).printJson(eq("{ \"version\": 1 }"));
    }

    @Test
    void execute_exceptionThrown_printError() throws ApiException {
        final CreateSchemaTask task = new CreateSchemaTask(outputFormatter,
                schemasApi,
                "test-1",
                "JSON",
                null,
                false,
                false,
                ByteBuffer.wrap(new byte[]{}));
        when(schemasApi.createSchema(any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter).printApiException(any(), any());
    }
}
