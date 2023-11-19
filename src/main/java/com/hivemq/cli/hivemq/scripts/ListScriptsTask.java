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

package com.hivemq.cli.hivemq.scripts;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.Script;
import com.hivemq.cli.openapi.hivemq.ScriptList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListScriptsTask {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubScriptsApi scriptsApi;
    private final @Nullable String @Nullable [] functionTypes;
    private final @Nullable String @Nullable [] scriptIds;
    private final @Nullable String @Nullable [] fields;
    private final @Nullable Integer limit;

    public ListScriptsTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubScriptsApi scriptApi,
            final @Nullable String @Nullable [] functionTypes,
            final @Nullable String @Nullable [] scriptIds,
            final @Nullable String @Nullable [] fields,
            final @Nullable Integer limit) {
        this.outputFormatter = outputFormatter;
        this.scriptsApi = scriptApi;
        this.functionTypes = functionTypes;
        this.scriptIds = scriptIds;
        this.limit = limit;
        this.fields = fields;
    }

    public boolean execute() {
        final String fieldsQueryParam;
        if (fields == null) {
            fieldsQueryParam = null;
        } else {
            fieldsQueryParam = String.join(",", fields);
        }

        final String scriptIdsQueryParam;
        if (scriptIds == null) {
            scriptIdsQueryParam = null;
        } else {
            scriptIdsQueryParam = String.join(",", scriptIds);
        }

        final String functionTypesQueryParam;
        if (functionTypes == null) {
            functionTypesQueryParam = null;
        } else {
            functionTypesQueryParam = String.join(",", functionTypes);
        }

        List<Script> allScripts = new ArrayList<>();

        try {
            String nextCursor = null;
            do {
                final ScriptList scriptList = scriptsApi.getAllScripts(fieldsQueryParam,
                        functionTypesQueryParam,
                        scriptIdsQueryParam,
                        50,
                        nextCursor);
                final List<Script> scripts = scriptList.getItems();
                final PaginationCursor links = scriptList.getLinks();

                if (scripts != null) {
                    allScripts.addAll(scripts);
                }

                if (limit != null && allScripts.size() >= limit) {
                    allScripts = allScripts.stream().limit(limit).collect(Collectors.toList());
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
            outputFormatter.printApiException("Failed to list scripts", apiException);
            return false;
        }

        final ScriptList scriptList = new ScriptList().items(allScripts);
        outputFormatter.printJson(scriptList);

        return true;
    }

}
