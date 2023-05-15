package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;

public class GetPolicyTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull PoliciesApi policiesApi;
    private final @NotNull String policyId;

    public GetPolicyTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull PoliciesApi policiesApi,
            final @NotNull String policyId) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.policyId = policyId;
    }

    public boolean execute() {
        final Policy policy;
        try {
            policy = policiesApi.getPolicy(policyId, null);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to get policy", apiException);
            return false;
        }

        outputFormatter.printJson(policy);

        return true;
    }
}
