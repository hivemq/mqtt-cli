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

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchemaDeleteCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull DataHubSchemasApi schemasApi = mock(DataHubSchemasApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new SchemaDeleteCommand(hiveMQRestService, outputFormatter));

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getSchemasApi(any(), anyDouble())).thenReturn(schemasApi);
    }

    @Test
    void call_idMissing_error() {
        assertEquals(2, commandLine.execute());
    }

    @Test
    void call_idEmpty_error() {
        assertEquals(1, commandLine.execute("--id="));
        verify(outputFormatter).printError(eq("The schema id must not be empty."));
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--id=schema-1"));
        verify(hiveMQRestService).getSchemasApi(eq("test-url"), eq(123d));
    }

    @Test
    void call_taskSuccessful_return0() throws ApiException {
        assertEquals(0, commandLine.execute("--id=schema-1"));
        verify(schemasApi).deleteSchema(eq("schema-1"));
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        doThrow(ApiException.class).when(schemasApi).deleteSchema(any());
        assertEquals(1, commandLine.execute("--id=schema-1"));
    }
}
