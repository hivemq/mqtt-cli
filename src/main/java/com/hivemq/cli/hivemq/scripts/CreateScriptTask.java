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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiScript;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Base64;

public class CreateScriptTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubScriptsApi scriptsApi;
    private final @NotNull String scriptId;
    private final @NotNull HivemqOpenapiScript.FunctionTypeEnum functionType;
    private final @NotNull String description;
    private final @NotNull ByteBuffer definition;
    private final boolean printVersion;

    public CreateScriptTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubScriptsApi scriptsApi,
            final @NotNull String scriptId,
            final @NotNull HivemqOpenapiScript.FunctionTypeEnum functionType,
            final @NotNull String description,
            final boolean printVersion,
            final @NotNull ByteBuffer definition) {
        this.outputFormatter = outputFormatter;
        this.scriptsApi = scriptsApi;
        this.scriptId = scriptId;
        this.functionType = functionType;
        this.description = description;
        this.definition = definition;
        this.printVersion = printVersion;
    }

    public boolean execute() {
        final String definitionBase64 = Base64.getEncoder().encodeToString(definition.array());
        final HivemqOpenapiScript script = new HivemqOpenapiScript().id(scriptId)
                .functionType(functionType)
                .description(description)
                .source(definitionBase64);

        final HivemqOpenapiScript createdScript;
        try {
            createdScript = scriptsApi.createScript(script);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to create script", apiException);
            return false;
        }

        if (printVersion) {
            final JsonObject versionObject = new JsonObject();
            if (createdScript.getVersion() != null) {
                versionObject.add(HivemqOpenapiScript.SERIALIZED_NAME_VERSION,
                        new JsonPrimitive(createdScript.getVersion()));
            }
            outputFormatter.printJson(versionObject);
        }

        return true;
    }


}
