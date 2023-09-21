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
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import org.jetbrains.annotations.NotNull;

public class BehaviorPolicyDeleteTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi;
    private final @NotNull String policyId;

    public BehaviorPolicyDeleteTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi,
            final @NotNull String policyId) {
        this.outputFormatter = outputFormatter;
        this.behaviorPoliciesApi = behaviorPoliciesApi;
        this.policyId = policyId;
    }

    public boolean execute() {
        try {
            behaviorPoliciesApi.deleteBehaviorPolicy(policyId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to delete behavior policy", apiException);
            return false;
        }

        return true;
    }
}
