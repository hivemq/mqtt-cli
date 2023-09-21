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

package com.hivemq.cli.commands.hivemq.behaviorpolicy;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.openapi.hivemq.BehaviorPolicyList;
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

public class BehaviorPolicyListCommandTest {

    private final @NotNull HiveMQRestService hiveMQRestService = mock(HiveMQRestService.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi = mock(DataHubBehaviorPoliciesApi.class);

    private final @NotNull CommandLine commandLine =
            new CommandLine(new BehaviorPolicyListCommand(hiveMQRestService, outputFormatter));

    @BeforeEach
    void setUp() throws ApiException {
        TestLoggerUtils.resetLogger();
        when(hiveMQRestService.getBehaviorPoliciesApi(any(), anyDouble())).thenReturn(behaviorPoliciesApi);
        final BehaviorPolicyList policyList = new BehaviorPolicyList();
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), any())).thenReturn(policyList);
    }

    @Test
    void call_noArguments_success() {
        assertEquals(0, commandLine.execute());
    }

    @Test
    void call_urlAndRateLimitPassed_usedInApi() {
        assertEquals(0, commandLine.execute("--rate=123", "--url=test-url", "--id=policy-1"));
        verify(hiveMQRestService).getBehaviorPoliciesApi(eq("test-url"), eq(123d));
    }

    // TODO
    @Test
    void call_multiplePolicyIdsAndClientIds_success() throws ApiException {
        assertEquals(0, commandLine.execute("--id=p1", "--id=p2", "--client-id=c1", "--client-id=c2"));
        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), eq("p1,p2"), eq("c1,c2"), any(), any());
    }

    @Test
    void call_multipleTopics_error() {
        assertEquals(2, commandLine.execute("--topic=t1", "--topic=t2"));
    }

    @Test
    void call_limitPositive_success() throws ApiException {
        assertEquals(0, commandLine.execute("--limit=5"));
    }

    @Test
    void call_limitNegative_error() {
        assertEquals(1, commandLine.execute("--limit=-1"));
        verify(outputFormatter).printError(eq("The limit must not be negative."));
    }

    @Test
    void call_multipleFields_success() throws ApiException {
        assertEquals(0, commandLine.execute("--field=id", "--field=version"));
        verify(behaviorPoliciesApi).getAllBehaviorPolicies(eq("id,version"), isNull(), isNull(), any(), any());
    }

    @Test
    void call_taskFailed_return1() throws ApiException {
        doThrow(ApiException.class).when(behaviorPoliciesApi).getAllBehaviorPolicies(any(), any(), any(), any(), any());
        assertEquals(1, commandLine.execute());
    }
}
