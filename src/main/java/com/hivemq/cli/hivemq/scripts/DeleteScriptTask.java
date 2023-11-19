package com.hivemq.cli.hivemq.scripts;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import org.jetbrains.annotations.NotNull;

public class DeleteScriptTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubScriptsApi scriptsApi;
    private final @NotNull String scriptId;

    public DeleteScriptTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubScriptsApi scriptsApi,
            final @NotNull String scriptId) {
        this.outputFormatter = outputFormatter;
        this.scriptsApi = scriptsApi;
        this.scriptId = scriptId;
    }

    public boolean execute() {
        try {
            scriptsApi.deleteScript(scriptId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to delete script", apiException);
            return false;
        }

        return true;
    }
}
