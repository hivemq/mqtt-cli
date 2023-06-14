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

package com.hivemq.cli.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.PaginationCursor;
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemaList;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListSchemasTask {

    private static final @NotNull Pattern CURSOR_PATTERN = Pattern.compile("cursor=([^&]*)");

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull SchemasApi schemasApi;
    private final @Nullable String @Nullable [] schemaTypes;
    private final @Nullable String @Nullable [] schemaIds;

    public ListSchemasTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull SchemasApi schemasApi,
            final @Nullable String @Nullable [] schemaTypes,
            final @Nullable String @Nullable [] schemaIds) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaTypes = schemaTypes;
        this.schemaIds = schemaIds;
    }

    public boolean execute() {
        final String schemaIdsQueryParam;
        if (schemaIds == null) {
            schemaIdsQueryParam = null;
        } else {
            schemaIdsQueryParam = String.join(",", schemaIds);
        }

        final String schemaTypesQueryParam;
        if (schemaTypes == null) {
            schemaTypesQueryParam = null;
        } else {
            schemaTypesQueryParam = String.join(",", schemaTypes);
        }

        final List<Schema> allSchemas = new ArrayList<>();

        try {
            String nextCursor = null;
            do {
                final SchemaList schemaList =
                        schemasApi.getAllSchemas(null, schemaTypesQueryParam, schemaIdsQueryParam, 50, nextCursor);
                final List<Schema> schemas = schemaList.getItems();
                final PaginationCursor links = schemaList.getLinks();

                if (schemas != null) {
                    allSchemas.addAll(schemas);
                }

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
            } while(nextCursor != null);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to list schemas", apiException);
            return false;
        }

        final SchemaList schemaList = new SchemaList().items(allSchemas);
        outputFormatter.printJson(schemaList);

        return true;
    }
}
