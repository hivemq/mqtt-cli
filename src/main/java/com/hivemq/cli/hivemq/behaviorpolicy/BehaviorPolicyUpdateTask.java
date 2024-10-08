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
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiBehaviorPolicy;
import org.jetbrains.annotations.NotNull;

public class BehaviorPolicyUpdateTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi;
    private final @NotNull Gson gson;
    private final @NotNull String policyId;
    private final @NotNull String definition;

    public BehaviorPolicyUpdateTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi,
            final @NotNull Gson gson,
            final @NotNull String policyId,
            final @NotNull String definition) {
        this.outputFormatter = outputFormatter;
        this.behaviorPoliciesApi = behaviorPoliciesApi;
        this.gson = gson;
        this.policyId = policyId;
        this.definition = definition;
    }

    public boolean execute() {
        final HivemqOpenapiBehaviorPolicy policy;
        try {
            policy = gson.fromJson(definition, HivemqOpenapiBehaviorPolicy.class);
        } catch (final JsonSyntaxException jsonSyntaxException) {
            outputFormatter.printError("Could not parse behavior policy JSON: " + jsonSyntaxException.getMessage());
            return false;
        }

        try {
            behaviorPoliciesApi.updateBehaviorPolicy(policyId, policy);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to update behavior policy", apiException);
            return false;
        }

        return true;
    }
}
