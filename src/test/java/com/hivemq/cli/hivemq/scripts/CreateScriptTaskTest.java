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

package com.hivemq.cli.hivemq.scripts;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.Script;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateScriptTaskTest {

    private final @NotNull DataHubScriptsApi scriptsApi = mock(DataHubScriptsApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock();

    @SuppressWarnings("FieldCanBeLocal")
    private final @NotNull String SCRIPT_DEFINITION = "function transform(person) { return 'hello ' + person }";

    @BeforeEach
    void setUp() throws ApiException {
        when(scriptsApi.createScript(any())).thenReturn(new Script());
    }

    @Test
    void execute_validScript_created() throws ApiException {
        final CreateScriptTask task = new CreateScriptTask(outputFormatter,
                scriptsApi,
                "script-1",
                Script.FunctionTypeEnum.TRANSFORMATION,
                "Sample Script",
                false,
                ByteBuffer.wrap(SCRIPT_DEFINITION.getBytes()));

        final ArgumentCaptor<Script> scriptCaptor = ArgumentCaptor.forClass(Script.class);

        assertTrue(task.execute());

        verify(scriptsApi, times(1)).createScript(scriptCaptor.capture());
        final Script createdScript = scriptCaptor.getValue();
        assertEquals("script-1", createdScript.getId());
        assertEquals(Script.FunctionTypeEnum.TRANSFORMATION, createdScript.getFunctionType());
        assertEquals("Sample Script", createdScript.getDescription());
        final String createdScriptDefinition = new String(Base64.getDecoder().decode(createdScript.getSource()));
        assertEquals(SCRIPT_DEFINITION, createdScriptDefinition);
    }

    @Test
    void execute_exceptionThrown_printError() throws ApiException {
        final CreateScriptTask task = new CreateScriptTask(outputFormatter,
                scriptsApi,
                "script-1",
                Script.FunctionTypeEnum.TRANSFORMATION,
                "Sample Script",
                false,
                ByteBuffer.wrap(new byte[]{}));
        when(scriptsApi.createScript(any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter).printApiException(any(), any());
    }

    @Test
    void execute_printVersion_printsVersion() throws ApiException {
        final CreateScriptTask task = new CreateScriptTask(outputFormatter,
                scriptsApi,
                "script-1",
                Script.FunctionTypeEnum.TRANSFORMATION,
                "Sample Script",
                true,
                ByteBuffer.wrap(SCRIPT_DEFINITION.getBytes()));

        final ArgumentCaptor<Script> scriptCaptor = ArgumentCaptor.forClass(Script.class);

        assertTrue(task.execute());

        verify(scriptsApi, times(1)).createScript(scriptCaptor.capture());
        final Script createdScript = scriptCaptor.getValue();
        assertEquals("script-1", createdScript.getId());
        assertEquals(Script.FunctionTypeEnum.TRANSFORMATION, createdScript.getFunctionType());
        assertEquals("Sample Script", createdScript.getDescription());
        final String createdScriptDefinition = new String(Base64.getDecoder().decode(createdScript.getSource()));
        assertEquals(SCRIPT_DEFINITION, createdScriptDefinition);

        final JsonObject versionObject = new JsonObject();
        if (createdScript.getVersion() != null) {
            versionObject.add(Script.SERIALIZED_NAME_VERSION, new JsonPrimitive(createdScript.getVersion()));
        }
        verify(outputFormatter).printJson(versionObject);
    }
}
