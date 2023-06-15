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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import com.hivemq.cli.utils.json.OffsetDateTimeSerializer;
import com.hivemq.cli.utils.json.SchemaSerializer;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

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

    private final @NotNull SchemasApi schemasApi = mock(SchemasApi.class);
    private @NotNull OutputFormatter outputFormatter;
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

    private final int API_RESPONSE_SCHEMA_VERSION = 5;
    private final @NotNull String API_RESPONSE_SCHEMA_JSON = "{" +
            "\"id\":\"schema-id\"," +
            "\"version\":" +
            API_RESPONSE_SCHEMA_VERSION +
            "," +
            "\"createdAt\":\"2020-01-02T03:04:05.006Z\"," +
            "\"type\":\"JSON\"," +
            "\"schemaDefinition\":\"abcdefg\"," +
            "\"arguments\":{}" +
            "}";

    @BeforeEach
    void setUp() {
        final Gson gson = new GsonBuilder().disableHtmlEscaping()
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
                .registerTypeAdapter(Schema.class, new SchemaSerializer())
                .create();
        outputFormatter = new OutputFormatter(new PrintStream(outputStream), gson);
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

        final String consoleOutput = outputStream.toString();
        assertEquals("{\"version\":" + API_RESPONSE_SCHEMA_VERSION + "}", consoleOutput.trim());
    }

    @Test
    void execute_printEntireSchemaSet_printWholeSchema() throws ApiException {
        final Schema schema = openapiSerialization.deserialize(API_RESPONSE_SCHEMA_JSON, Schema.class);
        when(schemasApi.createSchema(any())).thenReturn(schema);

        final CreateSchemaTask task = new CreateSchemaTask(outputFormatter,
                schemasApi,
                "test-1",
                "JSON",
                null,
                false,
                true,
                ByteBuffer.wrap(new byte[]{}));

        assertTrue(task.execute());

        final String consoleOutput = outputStream.toString();
        assertEquals(API_RESPONSE_SCHEMA_JSON, consoleOutput.trim());
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
