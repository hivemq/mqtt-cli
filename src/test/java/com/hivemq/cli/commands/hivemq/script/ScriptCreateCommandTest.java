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

package com.hivemq.cli.commands.hivemq.script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.Script;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.cli.utils.json.ScriptSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptCreateCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private @NotNull OutputFormatter outputFormatter;
    private final @NotNull ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final @NotNull JSON openapiSerialization = new JSON();
    private final @NotNull DataHubScriptsApi scriptsApi = mock(DataHubScriptsApi.class);

    private @NotNull CommandLine commandLine;

    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String SCRIPT_DEFINITION = "function transform(person) { return 'hello ' + person }";

    @BeforeEach
    void setUp() throws ApiException {
        TestLoggerUtils.resetLogger();

        final Gson gson = new GsonBuilder().disableHtmlEscaping()
                .registerTypeAdapter(Script.class, new ScriptSerializer())
                .create();

        outputFormatter = spy(new OutputFormatter(new PrintStream(outputStream), gson));
        commandLine = new CommandLine(new ScriptCreateCommand(hiveMQRestService, outputFormatter));

        when(hiveMQRestService.getScriptsApi(any(), anyDouble())).thenReturn(scriptsApi);
        when(scriptsApi.createScript(any())).thenReturn(new Script());
    }

    @Test
    void call_idMissing_error() {
        assertEquals(2, commandLine.execute("--type=function", "--description=test", "--definition=" + SCRIPT_DEFINITION));
    }

    @Test
    void call_typeMissing_error() {
        assertEquals(2, commandLine.execute("--id=s1", "--description=test", "--definition=" + SCRIPT_DEFINITION));
    }

    @Test
    void call_definitionMissing_error() {
        assertEquals(2, commandLine.execute("--id=s1", "--type=function", "--description=test"));
    }

    @Test
    void call_descriptionMissing_error() {
        assertEquals(2, commandLine.execute("--id=s1", "--type=function", "--definition=" + SCRIPT_DEFINITION));
    }

    @Test
    void call_validScriptFile_success() throws IOException {
        final File definitionFile = File.createTempFile("script", ".js");
        Files.write(definitionFile.toPath(), SCRIPT_DEFINITION.getBytes());
        assertEquals(0,
                commandLine.execute("--id=s1",
                        "--type=transformation",
                        "--file=" + definitionFile.getAbsolutePath(),
                        "--description=test"));
        assertTrue(outputStream.toString().isEmpty());
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0,
                commandLine.execute("--rate=123",
                        "--url=test-url",
                        "--id=s1",
                        "--type=transformation",
                        "--description=test",
                        "--definition=" + SCRIPT_DEFINITION));
        verify(hiveMQRestService).getScriptsApi(eq("test-url"), eq(123d));
    }

    @Test
    void execute_printVersionSet_versionPrinted() throws ApiException {
        final @NotNull String apiScriptResponseJson = "{" +
                "\"id\":\"s1\"," +
                "\"version\":5," +
                "\"createdAt\":\"2020-01-02T03:04:05.006Z\"," +
                "\"type\":\"FUNCTION\"," +
                "\"scriptDefinition\":\"J2NvbnNvbGUubG9nKCdIZWxsbywgV29ybGQhJyk7\"," +
                "\"arguments\":{}" +
                "}";
        final Script createdScript = openapiSerialization.deserialize(apiScriptResponseJson, Script.class);
        when(scriptsApi.createScript(any())).thenReturn(createdScript);

        assertEquals(0,
                commandLine.execute("--id=s1",
                        "--type=transformation",
                        "--description=test",
                        "--definition='console.log(\"Hello, World!\")'",
                        "--print-version"));

        final String consoleOutput = outputStream.toString();
        assertEquals("{\"version\":5}", consoleOutput.trim());
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        when(scriptsApi.createScript(any())).thenThrow(ApiException.class);
        assertEquals(1,
                commandLine.execute("--id=s1",
                        "--type=transformation",
                        "--description=test",
                        "--definition=" + SCRIPT_DEFINITION));
    }
}
