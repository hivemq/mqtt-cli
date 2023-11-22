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

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.ScriptList;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptListCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock();
    private final @NotNull OutputFormatter outputFormatter = mock();
    private final @NotNull DataHubScriptsApi scriptsApi = mock();

    private final @NotNull CommandLine commandLine =
            new CommandLine(new ScriptListCommand(hiveMQRestService, outputFormatter));

    @BeforeEach
    void setUp() throws ApiException {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getScriptsApi(any(), anyDouble())).thenReturn(scriptsApi);
        when(scriptsApi.getAllScripts(any(), any(), any(), any(), any())).thenReturn(new ScriptList());
    }

    @Test
    void call_noArguments_success() {
        assertEquals(0, commandLine.execute());
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--id=script-1"));
        verify(hiveMQRestService).getScriptsApi(eq("test-url"), eq(123d));
    }

    @Test
    void call_typeInvalid_error() {
        assertEquals(2, commandLine.execute("--type=invalid"));
    }

    @Test
    void call_multipleTypesAndScriptIds_success() throws ApiException {
        assertEquals(0, commandLine.execute("--type=transformation", "--type=transformation", "--id=s1", "--id=s2"));
        verify(scriptsApi).getAllScripts(isNull(), eq("TRANSFORMATION,TRANSFORMATION"), eq("s1,s2"), any(), isNull());
    }

    @Test
    void call_limitPositive_success() {
        assertEquals(0, commandLine.execute("--limit=5"));
    }

    @Test
    void call_limitNegative_error() {
        assertEquals(1, commandLine.execute("--limit=-1"));
        verify(outputFormatter).printError(eq("The limit must not be negative."));
    }

    @Test
    void call_multipleFields_success() throws ApiException {
        assertEquals(0, commandLine.execute("--field=id", "--field=createdAt"));
        verify(scriptsApi).getAllScripts(eq("id,createdAt"), isNull(), isNull(), any(), isNull());
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        doThrow(ApiException.class).when(scriptsApi).getAllScripts(any(), any(), any(), any(), any());
        assertEquals(1, commandLine.execute());
    }
}
