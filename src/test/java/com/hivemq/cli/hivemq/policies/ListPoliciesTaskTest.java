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

package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import com.hivemq.cli.openapi.hivemq.PolicyList;
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

public class ListPoliciesTaskTest {

    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    @Test
    void execute_policyIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] policyIds = {"policy-1", "policy-2", "policy-3"};
        final ListPoliciesTask task = new ListPoliciesTask(outputFormatter, policiesApi, null, policyIds, null);

        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), any())).thenReturn(new PolicyList());

        assertTrue(task.execute());
        final String policyIdsParameter = "policy-1,policy-2,policy-3";
        verify(policiesApi, times(1)).getAllPolicies(isNull(),
                eq(policyIdsParameter),
                isNull(),
                isNull(),
                any(),
                any());
    }

    @Test
    void execute_schemaIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] schemaIds = {"schema-1", "schema-2", "schema-3"};
        final ListPoliciesTask task = new ListPoliciesTask(outputFormatter, policiesApi, null, null, schemaIds);

        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), any())).thenReturn(new PolicyList());

        assertTrue(task.execute());
        final String schemaIdsParameter = "schema-1,schema-2,schema-3";
        verify(policiesApi, times(1)).getAllPolicies(isNull(),
                isNull(),
                eq(schemaIdsParameter),
                isNull(),
                any(),
                any());
    }

    @Test
    void execute_topicProvided_usedAsUrlParameter() throws ApiException {
        final ListPoliciesTask task = new ListPoliciesTask(outputFormatter, policiesApi, "topic-1", null, null);

        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), any())).thenReturn(new PolicyList());

        assertTrue(task.execute());
        verify(policiesApi, times(1)).getAllPolicies(isNull(), isNull(), isNull(), eq("topic-1"), any(), any());
    }

    @Test
    void execute_cursorReturned_allPagesFetched() throws ApiException {
        final ListPoliciesTask task = new ListPoliciesTask(outputFormatter, policiesApi, null, null, null);

        final Policy policy1 = new Policy().id("policy-1");
        final Policy policy2 = new Policy().id("policy-2");
        final Policy policy3 = new Policy().id("policy-3");
        final Policy policy4 = new Policy().id("policy-4");

        final String cursorPrefix = "/api/v1/data-validation/policies?cursor=";
        final PolicyList page1 = new PolicyList().items(Collections.singletonList(policy1))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-1"));
        final PolicyList page2 = new PolicyList().items(Arrays.asList(policy2, policy3))
                .links(new PaginationCursor().next(cursorPrefix + "cursor-2"));
        final PolicyList page3 = new PolicyList().items(Collections.singletonList(policy4));
        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);
        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), eq("cursor-2"))).thenReturn(page3);

        assertTrue(task.execute());

        verify(policiesApi).getAllPolicies(isNull(), isNull(), isNull(), isNull(), any(), isNull());
        verify(policiesApi).getAllPolicies(isNull(), isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(policiesApi).getAllPolicies(isNull(), isNull(), isNull(), isNull(), any(), eq("cursor-2"));

        final ArgumentCaptor<PolicyList> outputCaptor = ArgumentCaptor.forClass(PolicyList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(policy1, policy2, policy3, policy4), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final ListPoliciesTask task = new ListPoliciesTask(outputFormatter, policiesApi, null, null, null);

        when(policiesApi.getAllPolicies(any(), any(), any(), any(), any(), any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
