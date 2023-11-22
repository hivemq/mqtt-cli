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

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.Script;
import com.hivemq.cli.openapi.hivemq.ScriptList;
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

public class ListScriptsTaskTest {

    private final @NotNull DataHubScriptsApi scriptsApi = mock(DataHubScriptsApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock();

    @Test
    void execute_scriptIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] scriptIds = {"script-1", "script-2", "script-3"};
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, null, scriptIds, null, null);

        when(scriptsApi.getAllScripts(any(), any(), any(), any(), any())).thenReturn(new ScriptList());

        assertTrue(task.execute());
        final String scriptIdsQueryParam = "script-1,script-2,script-3";
        verify(scriptsApi, times(1)).getAllScripts(isNull(), isNull(), eq(scriptIdsQueryParam), any(), isNull());
    }

    @Test
    void execute_functionTypesProvided_usedAsUrlParameter() throws ApiException {
        final String[] functionTypes = {"type-a", "type-b", "type-c"};
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, functionTypes, null, null, null);

        when(scriptsApi.getAllScripts(any(), any(), any(), any(), any())).thenReturn(new ScriptList());

        assertTrue(task.execute());
        final String functionTypesQueryParam = "type-a,type-b,type-c";
        verify(scriptsApi, times(1)).getAllScripts(isNull(), eq(functionTypesQueryParam), isNull(), any(), isNull());
    }

    @Test
    void execute_fieldsProvided_usedAsUrlParameter() throws ApiException {
        final String[] fields = {"id", "version", "createdAt"};
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, null, null, fields, null);

        when(scriptsApi.getAllScripts(any(), any(), any(), any(), any())).thenReturn(new ScriptList());

        assertTrue(task.execute());
        final String fieldsQueryParam = "id,version,createdAt";
        verify(scriptsApi, times(1)).getAllScripts(eq(fieldsQueryParam), isNull(), isNull(), any(), isNull());
    }

    @Test
    void execute_cursorReturned_allPagesFetched() throws ApiException {
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, null, null, null, null);

        final Script script1 = new Script().id("script-1");
        final Script script2 = new Script().id("script-2");
        final Script script3 = new Script().id("script-3");
        final Script script4 = new Script().id("script-4");

        final String cursorPrefix = "/api/v1/data-validation/scripts?cursor=";
        final ScriptList page1 = new ScriptList().items(Collections.singletonList(script1))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final ScriptList page2 = new ScriptList().items(Arrays.asList(script2, script3))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        final ScriptList page3 = new ScriptList().items(Collections.singletonList(script4));
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), eq("cursor-2"))).thenReturn(page3);

        assertTrue(task.execute());

        verify(scriptsApi).getAllScripts(isNull(), isNull(), isNull(), any(), isNull());
        verify(scriptsApi).getAllScripts(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(scriptsApi).getAllScripts(isNull(), isNull(), isNull(), any(), eq("cursor-2"));
        verify(scriptsApi, times(3)).getAllScripts(any(), any(), any(), any(), any());

        final ArgumentCaptor<ScriptList> outputCaptor = ArgumentCaptor.forClass(ScriptList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(script1, script2, script3, script4), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_cursorReturnedLimitSpecified_limitNotExceeded() throws ApiException {
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, null, null, null, 3);

        final Script script1 = new Script().id("script-1");
        final Script script2 = new Script().id("script-2");
        final Script script3 = new Script().id("script-3");
        final Script script4 = new Script().id("script-4");

        final String cursorPrefix = "/api/v1/data-validation/scripts?cursor=";
        final ScriptList page1 = new ScriptList().items(Arrays.asList(script1, script2))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final ScriptList page2 = new ScriptList().items(Arrays.asList(script3, script4))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);

        assertTrue(task.execute());

        verify(scriptsApi).getAllScripts(isNull(), isNull(), isNull(), any(), isNull());
        verify(scriptsApi).getAllScripts(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(scriptsApi, times(2)).getAllScripts(any(), any(), any(), any(), any());

        final ArgumentCaptor<ScriptList> outputCaptor = ArgumentCaptor.forClass(ScriptList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(script1, script2, script3), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final ListScriptsTask task = new ListScriptsTask(outputFormatter, scriptsApi, null, null, null, null);

        when(scriptsApi.getAllScripts(any(), any(), any(), any(), any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
