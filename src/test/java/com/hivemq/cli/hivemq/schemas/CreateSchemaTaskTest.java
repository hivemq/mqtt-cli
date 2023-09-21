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
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateSchemaTaskTest {

    private final @NotNull DataHubSchemasApi schemasApi = mock(DataHubSchemasApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

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

    @BeforeEach
    void setUp() throws ApiException {
        when(schemasApi.createSchema(any())).thenReturn(new Schema());
    }

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
