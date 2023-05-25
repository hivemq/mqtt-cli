package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;

public class DeletePolicyTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull PoliciesApi policiesApi;
    private final @NotNull String policyId;

    public DeletePolicyTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull PoliciesApi policiesApi,
            final @NotNull String policyId) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.policyId = policyId;
    }

    public boolean execute() {
        try {
            policiesApi.deletePolicy(policyId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to delete policy", apiException);
            return false;
        }

        return true;
    }
}
