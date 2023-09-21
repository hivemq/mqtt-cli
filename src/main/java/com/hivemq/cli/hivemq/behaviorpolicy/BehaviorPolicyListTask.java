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
import com.hivemq.cli.openapi.hivemq.BehaviorPolicyList;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BehaviorPolicyListTask {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi;
    private final @Nullable String @Nullable [] policyIds;
    private final @Nullable String @Nullable [] clientIds;
    private final @Nullable String @Nullable [] fields;
    private final @Nullable Integer limit;

    private static final int DEFAULT_PAGE_SIZE = 50;

    public BehaviorPolicyListTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubBehaviorPoliciesApi behaviorPoliciesApi,
            final @Nullable String @Nullable [] policyIds,
            final @Nullable String @Nullable [] clientIds,
            final @Nullable String @Nullable [] fields,
            final @Nullable Integer limit) {
        this.outputFormatter = outputFormatter;
        this.behaviorPoliciesApi = behaviorPoliciesApi;
        this.policyIds = policyIds;
        this.clientIds = clientIds;
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

        final String clientIdsQueryParam;
        if (clientIds == null) {
            clientIdsQueryParam = null;
        } else {
            clientIdsQueryParam = String.join(",", clientIds);
        }

        List<BehaviorPolicy> allPolicies = new ArrayList<>();

        try {
            String nextCursor = null;
            do {
                final BehaviorPolicyList policyList = behaviorPoliciesApi.getAllBehaviorPolicies(fieldsQueryParam,
                        policyIdsQueryParam,
                        clientIdsQueryParam,
                        DEFAULT_PAGE_SIZE,
                        nextCursor);
                final List<BehaviorPolicy> policies = policyList.getItems();
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
            outputFormatter.printApiException("Failed to list behavior policies", apiException);
            return false;
        }

        final BehaviorPolicyList policyList = new BehaviorPolicyList().items(allPolicies);

        outputFormatter.printJson(policyList);

        return true;
    }
}
