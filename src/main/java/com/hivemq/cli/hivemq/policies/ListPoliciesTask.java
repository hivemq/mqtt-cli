package com.hivemq.cli.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.openapi.hivemq.Policy;
import com.hivemq.cli.openapi.hivemq.PolicyList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListPoliciesTask {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull PoliciesApi policiesApi;
    private final @Nullable String topic;
    private final @Nullable String @Nullable [] policyIds;
    private final @Nullable String @Nullable [] schemaIds;

    public ListPoliciesTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull PoliciesApi policiesApi,
            final @Nullable String topic,
            final @Nullable String @Nullable [] policyIds,
            final @Nullable String @Nullable [] schemaIds) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.topic = topic;
        this.policyIds = policyIds;
        this.schemaIds = schemaIds;
    }

    public boolean execute() {
        final String policyIdsParameter;
        if (policyIds == null) {
            policyIdsParameter = null;
        } else {
            policyIdsParameter = String.join(",", policyIds);
        }

        final String schemaIdsParameter;
        if (schemaIds == null) {
            schemaIdsParameter = null;
        } else {
            schemaIdsParameter = String.join(",", schemaIds);
        }

        final List<Policy> allPolicies = new ArrayList<>();

        try {
            boolean hasNextCursor = true;
            String nextCursor = null;
            while (hasNextCursor) {
                final PolicyList policyList =
                        policiesApi.getAllPolicies(null, policyIdsParameter, schemaIdsParameter, topic, 50, nextCursor);
                final List<Policy> policies = policyList.getItems();
                final PaginationCursor links = policyList.getLinks();

                if (policies != null) {
                    allPolicies.addAll(policies);
                }

                if (links != null && links.getNext() != null) {
                    final Matcher m = CURSOR_PATTERN.matcher(links.getNext());
                    if (m.find()) {
                        nextCursor = m.group(1);
                    } else {
                        hasNextCursor = false;
                    }
                } else {
                    hasNextCursor = false;
                }
            }
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to list policies", apiException);
            return false;
        }

        final PolicyList policyList = new PolicyList().items(allPolicies);
        outputFormatter.printJson(policyList);

        return true;
    }
}
