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

package com.hivemq.cli.commands.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.PolicyList;
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

public class ListPoliciesCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new ListPoliciesCommand(hiveMQRestService, outputFormatter));

    @BeforeEach
    void setUp() throws ApiException {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getPoliciesApi(any(), anyDouble())).thenReturn(policiesApi);
        final PolicyList policyList = new PolicyList();
        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), any())).thenReturn(policyList);
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--id=policy-1"));
        verify(hiveMQRestService).getPoliciesApi(eq("test-url"), eq(123d));
    }

    @Test
    void call_topicAndMultiplePolicyIdsAndSchemaIds_success() throws ApiException {
        assertEquals(0, commandLine.execute("--topic=t", "--id=p1", "--id=p2", "--schema-id=s1", "--schema-id=s2"));
        verify(policiesApi).getAllPolicies(isNull(), eq("p1,p2"), eq("s1,s2"), eq("t"), any(), any());
    }

    @Test
    void call_multipleTopics_error() {
        assertEquals(2, commandLine.execute("--topic=t1", "--topic=t2"));
    }

    @Test
    void call_noArguments_success() {
        assertEquals(0, commandLine.execute());
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        doThrow(ApiException.class).when(policiesApi).getAllPolicies(any(), any(), any(), any(), any(), any());
        assertEquals(1, commandLine.execute());
    }
}