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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubSchemasApi;
import com.hivemq.cli.openapi.hivemq.Schema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateSchemaTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubSchemasApi schemasApi;
    private final @NotNull String schemaId;
    private final @NotNull String schemaType;
    private final @Nullable String messageType;
    private final boolean allowUnknown;
    private final @NotNull ByteBuffer definition;
    private final boolean printVersion;

    public CreateSchemaTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubSchemasApi schemasApi,
            final @NotNull String schemaId,
            final @NotNull String schemaType,
            final @Nullable String messageType,
            final boolean allowUnknown,
            final boolean printVersion,
            final @NotNull ByteBuffer definition) {
        this.outputFormatter = outputFormatter;
        this.schemasApi = schemasApi;
        this.schemaId = schemaId;
        this.schemaType = schemaType;
        this.messageType = messageType;
        this.allowUnknown = allowUnknown;
        this.definition = definition;
        this.printVersion = printVersion;
    }

    public boolean execute() {
        Map<String, String> arguments = null;
        if (schemaType.equals("PROTOBUF")) {
            arguments = new HashMap<>();

            arguments.put("messageType", Objects.requireNonNull(messageType));
            arguments.put("allowUnknownFields", String.valueOf(allowUnknown));
        }

        final String definitionBase64 = Base64.getEncoder().encodeToString(definition.array());

        final Schema schema =
                new Schema().id(schemaId).type(schemaType).schemaDefinition(definitionBase64).arguments(arguments);

        final Schema createdSchema;
        try {
            createdSchema = schemasApi.createSchema(schema);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to create schema", apiException);
            return false;
        }

        if (printVersion) {
            final JsonObject versionObject = new JsonObject();
            if (createdSchema.getVersion() != null) {
                versionObject.add(Schema.SERIALIZED_NAME_VERSION, new JsonPrimitive(createdSchema.getVersion()));
            }
            outputFormatter.printJson(versionObject);
        }

        return true;
    }
}
