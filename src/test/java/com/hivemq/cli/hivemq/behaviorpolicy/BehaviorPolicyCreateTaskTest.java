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

import com.google.gson.Gson;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.BehaviorPolicy;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
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

public class BehaviorPolicyCreateTaskTest {

    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi = mock(DataHubBehaviorPoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);
    private final @NotNull ArgumentCaptor<BehaviorPolicy> policyCaptor = ArgumentCaptor.forClass(BehaviorPolicy.class);
    private final @NotNull Gson gson = new Gson();

    private static final @NotNull String POLICY_ID = "policy-1";
    private static final @NotNull String POLICY_JSON = "{ \"id\": \"" +
            POLICY_ID +
            "\"," +
            "\"matching\": { \"clientIdRegex\": \".*\" }," +
            "\"behavior\": {\"id\": \"Publish.quota\",\"arguments\": {\"maxPublishes\": 2}}}";

    @Test
    void execute_validBehaviorPolicy_success() throws ApiException {
        final BehaviorPolicy policy = gson.fromJson(POLICY_JSON, BehaviorPolicy.class);

        final BehaviorPolicyCreateTask task =
                new BehaviorPolicyCreateTask(outputFormatter, behaviorPoliciesApi, gson, POLICY_JSON);

        when(behaviorPoliciesApi.createBehaviorPolicy(policyCaptor.capture())).thenReturn(policy);

        assertTrue(task.execute());
        verify(behaviorPoliciesApi, times(1)).createBehaviorPolicy(policy);
        verify(outputFormatter, times(0)).printJson(any());
        assertEquals(policy, policyCaptor.getValue());
    }

    @Test
    void execute_invalidDefinition_printError() throws ApiException {
        final BehaviorPolicyCreateTask task =
                new BehaviorPolicyCreateTask(outputFormatter, behaviorPoliciesApi, gson, "invalid");

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printError(any());
        verify(behaviorPoliciesApi, times(0)).createBehaviorPolicy(any());
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final BehaviorPolicyCreateTask task =
                new BehaviorPolicyCreateTask(outputFormatter, behaviorPoliciesApi, gson, POLICY_JSON);

        when(behaviorPoliciesApi.createBehaviorPolicy(any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
        verify(behaviorPoliciesApi, times(1)).createBehaviorPolicy(any());
    }

}
