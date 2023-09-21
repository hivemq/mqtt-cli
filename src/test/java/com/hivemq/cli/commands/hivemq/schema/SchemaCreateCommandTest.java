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

package com.hivemq.cli.commands.hivemq.schema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.cli.utils.json.OffsetDateTimeSerializer;
import com.hivemq.cli.utils.json.SchemaSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchemaCreateCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private @NotNull OutputFormatter outputFormatter;
    private final @NotNull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final @NotNull JSON openapiSerialization = new JSON();
    private final @NotNull DataHubSchemasApi schemasApi = mock(DataHubSchemasApi.class);

    private @NotNull CommandLine commandLine;

    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String JSON_SCHEMA_DEFINITION = "{ \"type\": \"object\" }";

    // test.proto:
    // ```
    // syntax = "proto3";
    // message Test {}
    // ```
    // Created with `protoc -o /dev/stdout | base64`
    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String PROTOBUF_SCHEMA_DEFINITION = "ChwKCnRlc3QucHJvdG8iBgoEVGVzdGIGcHJvdG8z";


    @BeforeEach
    void setUp() throws ApiException {
        TestLoggerUtils.resetLogger();

        final Gson gson = new GsonBuilder().disableHtmlEscaping()
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
                .registerTypeAdapter(Schema.class, new SchemaSerializer())
                .create();

        outputFormatter = spy(new OutputFormatter(new PrintStream(outputStream), gson));
        commandLine = new CommandLine(new SchemaCreateCommand(hiveMQRestService, outputFormatter));

        when(hiveMQRestService.getSchemasApi(any(), anyDouble())).thenReturn(schemasApi);
        when(schemasApi.createSchema(any())).thenReturn(new Schema());
    }

    @Test
    void call_idMissing_error() {
        assertEquals(2, commandLine.execute("--type=json", "--definition=" + JSON_SCHEMA_DEFINITION));
    }

    @Test
    void call_typeMissing_error() {
        assertEquals(2, commandLine.execute("--id=s1", "--definition=" + JSON_SCHEMA_DEFINITION));
    }

    @Test
    void call_typeInvalid_error() {
        assertEquals(2, commandLine.execute("--id=s1", "--type=invalid", "--definition=" + JSON_SCHEMA_DEFINITION));
    }

    @Test
    void call_definitionMissing_error() {
        assertEquals(2, commandLine.execute("--id=s1", "type=json"));
    }

    @Test
    void call_validProtobufFile_success() throws IOException {
        final File definitionFile = File.createTempFile("schema", ".desc");
        final byte[] definitionBytes = Base64.getDecoder().decode(PROTOBUF_SCHEMA_DEFINITION);
        Files.write(definitionFile.toPath(), definitionBytes);
        assertEquals(0,
                commandLine.execute("--id=s1",
                        "--type=protobuf",
                        "--file=" + definitionFile.getAbsolutePath(),
                        "--message-type=Test"));
        assertTrue(outputStream.toString().isEmpty());
    }

    @Test
    void call_taskFailedOnProtoFile_hintProvided() throws IOException, ApiException {
        final File definitionFile = File.createTempFile("schema", ".proto");
        when(schemasApi.createSchema(any())).thenThrow(ApiException.class);
        assertEquals(1,
                commandLine.execute("--id=s1",
                        "--type=protobuf",
                        "--file=" + definitionFile.getAbsolutePath(),
                        "--message-type=Test"));
        verify(outputFormatter).printError(
                "Hint: the provided definition file must be a compiled descriptor file (.desc), not a .proto file.");
    }

    @Test
    void call_validJsonFile_success() throws IOException {
        final File definitionFile = File.createTempFile("schema", ".json");
        Files.write(definitionFile.toPath(), JSON_SCHEMA_DEFINITION.getBytes());
        assertEquals(0, commandLine.execute("--id=s1", "--type=json", "--file=" + definitionFile.getAbsolutePath()));
        assertTrue(outputStream.toString().isEmpty());
    }

    @Test
    void call_protobufMessageTypeMissing_error() {
        assertEquals(1, commandLine.execute("--id=s1", "--type=protobuf", "--definition=123"));
        verify(outputFormatter).printError(eq("Protobuf message type is missing. Option '--message-type' is not set."));
    }

    @Test
    void call_messageTypeWithJsonType_error() {
        assertEquals(1,
                commandLine.execute("--id=s1",
                        "--type=json",
                        "--definition=" + JSON_SCHEMA_DEFINITION,
                        "--message-type=Test"));
        verify(outputFormatter).printError(eq("Option '--message-type' is not applicable to schemas of type 'JSON'."));
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0,
                commandLine.execute("--rate=123",
                        "--url=test-url",
                        "--id=s1",
                        "--type=json",
                        "--definition=" + JSON_SCHEMA_DEFINITION));
        verify(hiveMQRestService).getSchemasApi(eq("test-url"), eq(123d));
    }

    @Test
    void execute_printVersionSet_versionPrinted() throws ApiException {
        final @NotNull String apiSchemaResponseJson = "{" +
                "\"id\":\"s1\"," +
                "\"version\":5," +
                "\"createdAt\":\"2020-01-02T03:04:05.006Z\"," +
                "\"type\":\"JSON\"," +
                "\"schemaDefinition\":\"J3t9Jw==\"," +
                "\"arguments\":{}" +
                "}";
        final Schema createdSchema = openapiSerialization.deserialize(apiSchemaResponseJson, Schema.class);
        when(schemasApi.createSchema(any())).thenReturn(createdSchema);

        assertEquals(0, commandLine.execute("--id=s1", "--type=json", "--definition='{}'", "--print-version"));

        final String consoleOutput = outputStream.toString();
        assertEquals("{\"version\":5}", consoleOutput.trim());
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        when(schemasApi.createSchema(any())).thenThrow(ApiException.class);
        assertEquals(1, commandLine.execute("--id=s1", "--type=json", "--definition=" + JSON_SCHEMA_DEFINITION));
    }
}
