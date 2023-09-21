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

package com.hivemq.cli.commands.hivemq.datapolicy;

import com.google.gson.Gson;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubDataPoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import com.hivemq.cli.utils.TestLoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataPolicyCreateCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull Gson gson = new Gson();
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull DataHubDataPoliciesApi policiesApi = mock(DataHubDataPoliciesApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new DataPolicyCreateCommand(hiveMQRestService, outputFormatter, gson));

    private static final @NotNull String POLICY_JSON =
            "{ \"id\": \"policy-1\", \"matching\": { \"topicFilter\": \"a/#\" } }";

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getDataPoliciesApi(any(), anyDouble())).thenReturn(policiesApi);
    }

    @Test
    void call_definitionMissing_error() {
        assertEquals(2, commandLine.execute());
    }

    @Test
    void call_argumentDefinitionEmpty_error() {
        assertEquals(1, commandLine.execute("--definition="));
        verify(outputFormatter).printError(eq("The policy definition must not be empty."));
    }

    @Test
    void call_fileDefinitionEmpty_error() throws IOException {
        final File policyFile = File.createTempFile("policy", ".json");
        Files.write(policyFile.toPath(), "".getBytes());
        assertEquals(1, commandLine.execute("--file=" + policyFile.getAbsolutePath()));
        verify(outputFormatter).printError(eq("The policy definition must not be empty."));
    }

    @Test
    void call_bothFileAndArgumentDefinition_error() throws IOException {
        final File policyFile = File.createTempFile("policy", ".json");
        assertEquals(2, commandLine.execute("--definition='abc'", "--file=" + policyFile.getAbsolutePath()));
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--definition=" + POLICY_JSON));
        verify(hiveMQRestService).getDataPoliciesApi(eq("test-url"), eq(123d));
    }

    @Test
    void call_taskSuccessful_return0() {
        assertEquals(0, commandLine.execute("--definition=" + POLICY_JSON));
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        when(policiesApi.createDataPolicy(any())).thenThrow(ApiException.class);
        assertEquals(1, commandLine.execute("--definition=" + POLICY_JSON));
    }
}
