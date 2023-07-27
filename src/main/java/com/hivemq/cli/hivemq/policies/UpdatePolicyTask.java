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
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataGovernanceHubPoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;

public class UpdatePolicyTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataGovernanceHubPoliciesApi policiesApi;
    private final @NotNull Gson gson;
    private final @NotNull String policyId;
    private final @NotNull String definition;

    public UpdatePolicyTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataGovernanceHubPoliciesApi policiesApi,
            final @NotNull Gson gson,
            final @NotNull String policyId,
            final @NotNull String definition) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.gson = gson;
        this.policyId = policyId;
        this.definition = definition;
    }

    public boolean execute() {
        final Policy policy;
        try {
            policy = gson.fromJson(definition, Policy.class);
        } catch (final JsonSyntaxException jsonSyntaxException) {
            outputFormatter.printError("Could not parse policy JSON: " + jsonSyntaxException.getMessage());
            return false;
        }

        try {
            policiesApi.updatePolicy(policyId, policy);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to update policy", apiException);
            return false;
        }

        return true;
    }
}
