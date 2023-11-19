package com.hivemq.cli.hivemq.scripts;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.Script;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class CreateScriptTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubScriptsApi scriptsApi;
    private final @NotNull String scriptId;
    private final @NotNull String functionType;
    private final @NotNull String description;
    private final @NotNull ByteBuffer definition;
    private final boolean printVersion;

    public CreateScriptTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubScriptsApi scriptsApi,
            final @NotNull String scriptId,
            final @NotNull String functionType,
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
        final String definitionBase64 = java.util.Base64.getEncoder().encodeToString(definition.array());
        final Script script = new Script().id(scriptId).functionType(functionType).description(description).source(definitionBase64);

        System.out.println("script.getId() = " + script.getId());
        System.out.println("script.getFunctionType() = " + script.getFunctionType());

        final Script createdScript;
        try {
            createdScript = scriptsApi.createScript(script);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to create script", apiException);
            return false;
        }

        if (printVersion) {
            final JsonObject versionObject = new JsonObject();
            if (createdScript.getVersion() != null) {
                versionObject.add(Script.SERIALIZED_NAME_VERSION, new JsonPrimitive(createdScript.getVersion()));
            }
            outputFormatter.printJson(versionObject);
        }

        return true;
    }


}
