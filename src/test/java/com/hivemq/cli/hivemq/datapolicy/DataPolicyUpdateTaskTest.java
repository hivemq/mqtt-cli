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

import com.google.gson.Gson;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubDataPoliciesApi;
import com.hivemq.cli.openapi.hivemq.DataPolicy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataPolicyUpdateTaskTest {

    private final @NotNull DataHubDataPoliciesApi dataPoliciesApi = mock(DataHubDataPoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull ArgumentCaptor<DataPolicy> policyCaptor = ArgumentCaptor.forClass(DataPolicy.class);
    private final @NotNull Gson gson = new Gson();

    private static final @NotNull String POLICY_ID = "policy-1";
    private static final @NotNull String POLICY_JSON =
            "{ \"id\": \"" + POLICY_ID + "\", \"matching\": { \"topicFilter\": \"a/#\" } }";

    @Test
    void execute_validDataPolicy_success() throws ApiException {
        final DataPolicy policy = gson.fromJson(POLICY_JSON, DataPolicy.class);

        final DataPolicyUpdateTask task = new DataPolicyUpdateTask(outputFormatter, dataPoliciesApi,

                gson, POLICY_ID, POLICY_JSON);

        when(dataPoliciesApi.updateDataPolicy(eq(POLICY_ID), policyCaptor.capture())).thenReturn(policy);

        assertTrue(task.execute());
        verify(dataPoliciesApi, times(1)).updateDataPolicy(eq(POLICY_ID), eq(policy));
        verify(outputFormatter, times(0)).printJson(any());
        assertEquals(policy, policyCaptor.getValue());
    }

    @Test
    void execute_invalidDefinition_printError() throws ApiException {
        final DataPolicyUpdateTask task = new DataPolicyUpdateTask(outputFormatter, dataPoliciesApi,

                gson, POLICY_ID, "invalid");

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printError(any());
        verify(dataPoliciesApi, times(0)).updateDataPolicy(eq(POLICY_ID), any());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final DataPolicyUpdateTask task = new DataPolicyUpdateTask(outputFormatter, dataPoliciesApi,

                gson, POLICY_ID, POLICY_JSON);

        when(dataPoliciesApi.updateDataPolicy(eq(POLICY_ID), any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
        verify(dataPoliciesApi, times(1)).updateDataPolicy(eq(POLICY_ID), any());
    }

}
