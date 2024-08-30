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

package com.hivemq.cli.hivemq.datapolicy;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubDataPoliciesApi;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiDataPolicy;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiDataPolicyList;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiPaginationCursor;
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

public class DataPolicyListTaskTest {

    private final @NotNull DataHubDataPoliciesApi dataPoliciesApi = mock(DataHubDataPoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock();

    @Test
    void execute_policyIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] policyIds = {"policy-1", "policy-2", "policy-3"};
        final DataPolicyListTask task =
                new DataPolicyListTask(outputFormatter, dataPoliciesApi, null, policyIds, null, null, null);

        when(dataPoliciesApi.getAllDataPolicies(any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new HivemqOpenapiDataPolicyList());

        assertTrue(task.execute());
        final String policyIdsQueryParam = "policy-1,policy-2,policy-3";
        verify(dataPoliciesApi, times(1)).getAllDataPolicies(isNull(),
                eq(policyIdsQueryParam),
                isNull(),
                isNull(),
                any(),
                any());
    }

    @Test
    void execute_schemaIdsProvided_usedAsUrlParameter() throws ApiException {
        final String[] schemaIds = {"schema-1", "schema-2", "schema-3"};
        final DataPolicyListTask task = new DataPolicyListTask(outputFormatter, dataPoliciesApi,

                null, null, schemaIds, null, null);

        when(dataPoliciesApi.getAllDataPolicies(any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new HivemqOpenapiDataPolicyList());

        assertTrue(task.execute());
        final String schemaIdsQueryParam = "schema-1,schema-2,schema-3";
        verify(dataPoliciesApi, times(1)).getAllDataPolicies(isNull(),
                isNull(),
                eq(schemaIdsQueryParam),
                isNull(),
                any(),
                any());
    }

    @Test
    void execute_topicProvided_usedAsUrlParameter() throws ApiException {
        final DataPolicyListTask task = new DataPolicyListTask(outputFormatter, dataPoliciesApi,

                "topic-1", null, null, null, null);

        when(dataPoliciesApi.getAllDataPolicies(any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new HivemqOpenapiDataPolicyList());

        assertTrue(task.execute());
        verify(dataPoliciesApi, times(1)).getAllDataPolicies(isNull(), isNull(), isNull(), eq("topic-1"), any(), any());
    }

    @Test
    void execute_fieldsProvided_usedAsUrlParameter() throws ApiException {
        final String[] fields = {"id", "version", "createdAt"};
        final DataPolicyListTask task =
                new DataPolicyListTask(outputFormatter, dataPoliciesApi, null, null, null, fields, null);

        when(dataPoliciesApi.getAllDataPolicies(any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(new HivemqOpenapiDataPolicyList());

        assertTrue(task.execute());
        final String fieldsQueryParam = "id,version,createdAt";
        verify(dataPoliciesApi, times(1)).getAllDataPolicies(eq(fieldsQueryParam),
                isNull(),
                isNull(),
                isNull(),
                any(),
                any());
    }

    @Test
    void execute_cursorReturned_allPagesFetched() throws ApiException {
        final DataPolicyListTask task = new DataPolicyListTask(outputFormatter, dataPoliciesApi,

                null, null, null, null, null);

        final HivemqOpenapiDataPolicy policy1 = new HivemqOpenapiDataPolicy().id("policy-1");
        final HivemqOpenapiDataPolicy policy2 = new HivemqOpenapiDataPolicy().id("policy-2");
        final HivemqOpenapiDataPolicy policy3 = new HivemqOpenapiDataPolicy().id("policy-3");
        final HivemqOpenapiDataPolicy policy4 = new HivemqOpenapiDataPolicy().id("policy-4");

        final String cursorPrefix = "/api/v1/data-hub/data-validation/policies?cursor=";
        final HivemqOpenapiDataPolicyList page1 =
                new HivemqOpenapiDataPolicyList().items(Collections.singletonList(policy1))
                        .links(new HivemqOpenapiPaginationCursor().next(cursorPrefix + "cursor-1"));
        final HivemqOpenapiDataPolicyList page2 =
                new HivemqOpenapiDataPolicyList().items(Arrays.asList(policy2, policy3))
                        .links(new HivemqOpenapiPaginationCursor().next(cursorPrefix + "cursor-2"));
        final HivemqOpenapiDataPolicyList page3 =
                new HivemqOpenapiDataPolicyList().items(Collections.singletonList(policy4));
        when(dataPoliciesApi.getAllDataPolicies(any(), any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(dataPoliciesApi.getAllDataPolicies(any(), any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);
        when(dataPoliciesApi.getAllDataPolicies(any(), any(), any(), any(), any(), eq("cursor-2"))).thenReturn(page3);

        assertTrue(task.execute());

        verify(dataPoliciesApi).getAllDataPolicies(isNull(), isNull(), isNull(), isNull(), any(), isNull());
        verify(dataPoliciesApi).getAllDataPolicies(isNull(), isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(dataPoliciesApi).getAllDataPolicies(isNull(), isNull(), isNull(), isNull(), any(), eq("cursor-2"));
        verify(dataPoliciesApi, times(3)).getAllDataPolicies(any(), any(), any(), any(), any(), any());

        final ArgumentCaptor<HivemqOpenapiDataPolicyList> outputCaptor =
                ArgumentCaptor.forClass(HivemqOpenapiDataPolicyList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(policy1, policy2, policy3, policy4), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_cursorReturnedLimitSpecified_limitNotExceeded() throws ApiException {
        final DataPolicyListTask task = new DataPolicyListTask(outputFormatter, dataPoliciesApi,

                null, null, null, null, 3);

        final HivemqOpenapiDataPolicy policy1 = new HivemqOpenapiDataPolicy().id("policy-1");
        final HivemqOpenapiDataPolicy policy2 = new HivemqOpenapiDataPolicy().id("policy-2");
        final HivemqOpenapiDataPolicy policy3 = new HivemqOpenapiDataPolicy().id("policy-3");
        final HivemqOpenapiDataPolicy policy4 = new HivemqOpenapiDataPolicy().id("policy-4");

        final String cursorPrefix = "/api/v1/data-validation/schemas?cursor=";
        final HivemqOpenapiDataPolicyList page1 =
                new HivemqOpenapiDataPolicyList().items(Arrays.asList(policy1, policy2))
                        .links(new HivemqOpenapiPaginationCursor().next(cursorPrefix + "cursor-1"));
        final HivemqOpenapiDataPolicyList page2 =
                new HivemqOpenapiDataPolicyList().items(Arrays.asList(policy3, policy4))
                        .links(new HivemqOpenapiPaginationCursor().next(cursorPrefix + "cursor-2"));
        when(dataPoliciesApi.getAllDataPolicies(any(), any(), any(), any(), any(), isNull())).thenReturn(page1);
        when(dataPoliciesApi.getAllDataPolicies(any(), any(), any(), any(), any(), eq("cursor-1"))).thenReturn(page2);

        assertTrue(task.execute());

        verify(dataPoliciesApi).getAllDataPolicies(isNull(), isNull(), isNull(), isNull(), any(), isNull());
        verify(dataPoliciesApi).getAllDataPolicies(isNull(), isNull(), isNull(), isNull(), any(), eq("cursor-1"));
        verify(dataPoliciesApi, times(2)).getAllDataPolicies(any(), any(), any(), any(), any(), any());

        final ArgumentCaptor<HivemqOpenapiDataPolicyList> outputCaptor =
                ArgumentCaptor.forClass(HivemqOpenapiDataPolicyList.class);
        verify(outputFormatter).printJson(outputCaptor.capture());
        assertEquals(Arrays.asList(policy1, policy2, policy3), outputCaptor.getValue().getItems());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final DataPolicyListTask task = new DataPolicyListTask(outputFormatter, dataPoliciesApi,

                null, null, null, null, null);

        when(dataPoliciesApi.getAllDataPolicies(any(),
                any(),
                any(),
                any(),
                any(),
                any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }
}
