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

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataGovernanceHubPoliciesApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.Policy;
import com.hivemq.cli.openapi.hivemq.PolicyList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListPoliciesTask {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataGovernanceHubPoliciesApi policiesApi;
    private final @Nullable String topic;
    private final @Nullable String @Nullable [] policyIds;
    private final @Nullable String @Nullable [] schemaIds;
    private final @Nullable String @Nullable [] fields;
    private final @Nullable Integer limit;

    public ListPoliciesTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataGovernanceHubPoliciesApi policiesApi,
            final @Nullable String topic,
            final @Nullable String @Nullable [] policyIds,
            final @Nullable String @Nullable [] schemaIds,
            final @Nullable String @Nullable [] fields,
            final @Nullable Integer limit) {
        this.outputFormatter = outputFormatter;
        this.policiesApi = policiesApi;
        this.topic = topic;
        this.policyIds = policyIds;
        this.schemaIds = schemaIds;
        this.fields = fields;
        this.limit = limit;
    }

    public boolean execute() {
        final String fieldsQueryParam;
        if (fields == null) {
            fieldsQueryParam = null;
        } else {
            fieldsQueryParam = String.join(",", fields);
        }

        final String policyIdsQueryParam;
        if (policyIds == null) {
            policyIdsQueryParam = null;
        } else {
            policyIdsQueryParam = String.join(",", policyIds);
        }

        final String schemaIdsQueryParam;
        if (schemaIds == null) {
            schemaIdsQueryParam = null;
        } else {
            schemaIdsQueryParam = String.join(",", schemaIds);
        }

        List<Policy> allPolicies = new ArrayList<>();

        try {
            String nextCursor = null;
            do {
                final PolicyList policyList = policiesApi.getAllPolicies(fieldsQueryParam,
                        policyIdsQueryParam,
                        schemaIdsQueryParam,
                        topic,
                        50,
                        nextCursor);
                final List<Policy> policies = policyList.getItems();
                final PaginationCursor links = policyList.getLinks();

                if (policies != null) {
                    allPolicies.addAll(policies);
                }

                if (limit != null && allPolicies.size() >= limit) {
                    allPolicies = allPolicies.stream().limit(limit).collect(Collectors.toList());
                    nextCursor = null;
                } else {
                    if (links == null || links.getNext() == null) {
                        nextCursor = null;
                    } else {
                        final Matcher matcher = CURSOR_PATTERN.matcher(links.getNext());
                        if (!matcher.find()) {
                            nextCursor = null;
                        } else {
                            nextCursor = matcher.group(1);
                        }
                    }
                }
            } while (nextCursor != null);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to list policies", apiException);
            return false;
        }

        final PolicyList policyList = new PolicyList().items(allPolicies);
        outputFormatter.printJson(policyList);

        return true;
    }
}
