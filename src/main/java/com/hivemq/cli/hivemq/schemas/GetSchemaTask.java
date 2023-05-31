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
import com.hivemq.cli.openapi.hivemq.Schema;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import org.jetbrains.annotations.NotNull;

public class GetSchemaTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull SchemasApi schemasApi;
    private final @NotNull String schemaId;

    public GetSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull SchemasApi schemasApi,
            final @NotNull String schemaId) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
    }

    public boolean execute() {
        final Schema schema;
        try {
            schema = schemasApi.getSchema(schemaId, null);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to get schema", apiException);
            return false;
        }

        outputFormatter.printJson(schema);

        return true;
    }
}
