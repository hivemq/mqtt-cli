package com.hivemq.cli.hivemq.scripts;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.openapi.hivemq.Script;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GetScriptTask {

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubScriptsApi scriptsApi;
    private final @NotNull String scriptId;
    private final @Nullable String @Nullable [] fields;

    public GetScriptTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubScriptsApi scriptsApi,
            final @NotNull String scriptId,
            final @Nullable String @Nullable [] fields) {
        this.outputFormatter = outputFormatter;
        this.scriptsApi = scriptsApi;
        this.scriptId = scriptId;
        this.fields = fields;
    }

    public boolean execute() {
        final String fieldsQueryParam;
        if (fields == null) {
            fieldsQueryParam = null;
        } else {
            fieldsQueryParam = String.join(",", fields);
        }

        final Script script;
        try {
            script = scriptsApi.getScript(scriptId, fieldsQueryParam);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to get script", apiException);
            return false;
        }

        outputFormatter.printJson(script);

        return true;
    }
}
