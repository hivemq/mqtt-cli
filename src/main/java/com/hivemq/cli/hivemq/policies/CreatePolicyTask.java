package com.hivemq.cli.hivemq.policies;

import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;

public class CreatePolicyTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull PoliciesApi policiesApi;
    private final @NotNull String definition;

    public CreatePolicyTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull PoliciesApi policiesApi,
            final @NotNull String definition) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.definition = definition;
    }

    public boolean execute() {
        final Policy policy;
        try {
            policy = outputFormatter.getGson().fromJson(definition, Policy.class);
        } catch (final JsonSyntaxException jsonSyntaxException) {
            outputFormatter.printError("Could not parse policy JSON: " + jsonSyntaxException.getMessage());
            return false;
        }

        try {
            policiesApi.createPolicy(policy);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to create policy", apiException);
            return false;
        }

        outputFormatter.printJson(policy);

        return true;
    }
}
