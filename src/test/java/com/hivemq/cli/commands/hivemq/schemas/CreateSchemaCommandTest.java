package com.hivemq.cli.commands.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateSchemaCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull SchemasApi schemasApi = mock(SchemasApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new CreateSchemaCommand(hiveMQRestService, outputFormatter));


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
    void setUp() {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getSchemasApi(any(), anyDouble())).thenReturn(schemasApi);
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
        verify(outputFormatter).printError(eq("Option '--message-type' is not applicable to schemas of type 'json'."));
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
    void call_taskFailed_return1() throws ApiException {
        when(schemasApi.createSchema(any())).thenThrow(ApiException.class);
        assertEquals(1, commandLine.execute("--id=s1", "--type=json", "--definition=" + JSON_SCHEMA_DEFINITION));
    }
}
