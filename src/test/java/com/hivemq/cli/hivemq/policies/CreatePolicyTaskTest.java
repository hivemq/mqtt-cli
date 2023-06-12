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

import com.google.gson.Gson;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreatePolicyTaskTest {

    private final @NotNull PoliciesApi policiesApi = mock(PoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
    private final @NotNull Gson gson = new Gson();

    private static final @NotNull String POLICY_ID = "policy-1";
    private static final @NotNull String POLICY_JSON =
            "{ \"id\": \"" + POLICY_ID + "\", \"matching\": { \"topicFilter\": \"a/#\" } }";

    @Test
    void execute_validPolicy_success() throws ApiException {
        final Policy policy = gson.fromJson(POLICY_JSON, Policy.class);

        final CreatePolicyTask task = new CreatePolicyTask(outputFormatter, policiesApi, gson, POLICY_JSON);

        when(policiesApi.createPolicy(policyCaptor.capture())).thenReturn(policy);

        assertTrue(task.execute());
        verify(policiesApi, times(1)).createPolicy(policy);
        verify(outputFormatter, times(0)).printJson(any());
        assertEquals(policy, policyCaptor.getValue());
    }

    @Test
    void execute_invalidDefinition_printError() throws ApiException {
        final CreatePolicyTask task = new CreatePolicyTask(outputFormatter, policiesApi, gson, "invalid");

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printError(any());
        verify(policiesApi, times(0)).createPolicy(any());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final CreatePolicyTask task = new CreatePolicyTask(outputFormatter, policiesApi, gson, POLICY_JSON);

        when(policiesApi.createPolicy(any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
        verify(policiesApi, times(1)).createPolicy(any());
    }

}
