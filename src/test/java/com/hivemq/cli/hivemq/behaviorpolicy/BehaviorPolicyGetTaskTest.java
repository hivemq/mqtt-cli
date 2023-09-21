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
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BehaviorPolicyGetTaskTest {

    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi = mock(DataHubBehaviorPoliciesApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    private static final @NotNull String POLICY_ID = "policy-1";

    @Test
    void execute_validId_success() throws ApiException {
        final BehaviorPolicy policy = new BehaviorPolicy();

        final BehaviorPolicyGetTask task =
                new BehaviorPolicyGetTask(outputFormatter, behaviorPoliciesApi, POLICY_ID, null);

        when(behaviorPoliciesApi.getBehaviorPolicy(eq(POLICY_ID), isNull())).thenReturn(policy);

        assertTrue(task.execute());
        verify(behaviorPoliciesApi, times(1)).getBehaviorPolicy(eq(POLICY_ID), isNull());
        verify(outputFormatter, times(1)).printJson(policy);
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final BehaviorPolicyGetTask task =
                new BehaviorPolicyGetTask(outputFormatter, behaviorPoliciesApi, POLICY_ID, null);

        when(behaviorPoliciesApi.getBehaviorPolicy(any(), isNull())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }

}
