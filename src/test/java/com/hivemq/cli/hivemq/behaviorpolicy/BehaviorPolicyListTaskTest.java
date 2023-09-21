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

package com.hivemq.cli.hivemq.behaviorpolicy;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.BehaviorPolicy;
import com.hivemq.cli.openapi.hivemq.BehaviorPolicyList;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
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

public class BehaviorPolicyListTaskTest {

    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi = mock(DataHubBehaviorPoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    @Test
    void execute_policyIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] policyIds = {"policy-1", "policy-2", "policy-3"};
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, policyIds, null, null, null);

        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new BehaviorPolicyList());

        assertTrue(task.execute());
        final String policyIdsQueryParam = "policy-1,policy-2,policy-3";
        verify(behaviorPoliciesApi, times(1)).getAllBehaviorPolicies(isNull(),
                eq(policyIdsQueryParam),
                isNull(),
                any(),
                isNull());
    }

    @Test
    void execute_clientIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] clientIds = {"client-1", "client-2", "client-3"};
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, null, clientIds, null, null);

        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new BehaviorPolicyList());

        assertTrue(task.execute());
        final String clientIdsQueryParam = "client-1,client-2,client-3";
        verify(behaviorPoliciesApi, times(1)).getAllBehaviorPolicies(isNull(),
                isNull(),
                eq(clientIdsQueryParam),
                any(),
                isNull());
    }

    @Test
    void execute_fieldsProvided_usedAsUrlParameter() throws ApiException {
        final String[] fields = {"id", "version", "createdAt"};
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, null, null, fields, null);

        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new BehaviorPolicyList());

        assertTrue(task.execute());
        final String fieldsQueryParam = "id,version,createdAt";
        verify(behaviorPoliciesApi, times(1)).getAllBehaviorPolicies(eq(fieldsQueryParam),
                isNull(),
                isNull(),
                any(),
                isNull());
    }

    @Test
    void execute_cursorReturned_allPagesFetched() throws ApiException {
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, null, null, null, null);

        final BehaviorPolicy policy1 = new BehaviorPolicy().id("policy-1");
        final BehaviorPolicy policy2 = new BehaviorPolicy().id("policy-2");
        final BehaviorPolicy policy3 = new BehaviorPolicy().id("policy-3");
        final BehaviorPolicy policy4 = new BehaviorPolicy().id("policy-4");

        final String cursorPrefix = "/api/v1/data-validation/policies?cursor=";
        final BehaviorPolicyList page1 = new BehaviorPolicyList().items(Collections.singletonList(policy1))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final BehaviorPolicyList page2 = new BehaviorPolicyList().items(Arrays.asList(policy2, policy3))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        final BehaviorPolicyList page3 = new BehaviorPolicyList().items(Collections.singletonList(policy4));
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), eq("cursor-2"))).thenReturn(page3);

        assertTrue(task.execute());

        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), isNull(), isNull(), any(), isNull());
        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), isNull(), isNull(), any(), eq("cursor-2"));
        verify(behaviorPoliciesApi, times(3)).getAllBehaviorPolicies(any(), any(), any(), any(), any());

        final ArgumentCaptor<BehaviorPolicyList> outputCaptor = ArgumentCaptor.forClass(BehaviorPolicyList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(policy1, policy2, policy3, policy4), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_cursorReturnedLimitSpecified_limitNotExceeded() throws ApiException {
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, null, null, null, 3);

        final BehaviorPolicy policy1 = new BehaviorPolicy().id("policy-1");
        final BehaviorPolicy policy2 = new BehaviorPolicy().id("policy-2");
        final BehaviorPolicy policy3 = new BehaviorPolicy().id("policy-3");
        final BehaviorPolicy policy4 = new BehaviorPolicy().id("policy-4");

        final String cursorPrefix = "/api/v1/data-hub/behavior-validation/policies?cursor=";
        final BehaviorPolicyList page1 = new BehaviorPolicyList().items(Arrays.asList(policy1, policy2))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final BehaviorPolicyList page2 = new BehaviorPolicyList().items(Arrays.asList(policy3, policy4))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);

        assertTrue(task.execute());

        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), isNull(), isNull(), any(), isNull());
        verify(behaviorPoliciesApi).getAllBehaviorPolicies(isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(behaviorPoliciesApi, times(2)).getAllBehaviorPolicies(any(), any(), any(), any(), any());

        final ArgumentCaptor<BehaviorPolicyList> outputCaptor = ArgumentCaptor.forClass(BehaviorPolicyList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(policy1, policy2, policy3), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final BehaviorPolicyListTask task =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, null, null, null, null);

        when(behaviorPoliciesApi.getAllBehaviorPolicies(any(),
                any(),
                any(),
                any(),
                any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
