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

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import org.jetbrains.annotations.NotNull;

public class DeleteSchemaTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubSchemasApi schemasApi;
    private final @NotNull String schemaId;

    public DeleteSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubSchemasApi schemasApi,
            final @NotNull String schemaId) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
    }

    public boolean execute() {
        try {
            schemasApi.deleteSchema(schemaId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to delete schema", apiException);
            return false;
        }

        return true;
    }
}
