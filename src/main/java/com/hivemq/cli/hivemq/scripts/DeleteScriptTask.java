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
