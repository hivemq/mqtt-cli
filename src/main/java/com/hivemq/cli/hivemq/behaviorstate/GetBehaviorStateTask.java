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

package com.hivemq.cli.hivemq.behaviorstate;

import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.openapi.hivemq.DataHubDataPoliciesApi;
import com.hivemq.cli.openapi.hivemq.DataHubStateApi;
import com.hivemq.cli.openapi.hivemq.DataPolicy;
import com.hivemq.cli.openapi.hivemq.FsmStatesInformationListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GetBehaviorStateTask {
    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull DataHubStateApi dataHubStateApi;
    private final @NotNull String clientId;

    public GetBehaviorStateTask(
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull DataHubStateApi dataHubStateApi,
            final @NotNull String clientId) {
        this.outputFormatter = outputFormatter;
        this.dataHubStateApi = dataHubStateApi;
        this.clientId = clientId;
    }

    public boolean execute() {
        final FsmStatesInformationListItem clientStateList;

        try {
            clientStateList = dataHubStateApi.getClientState(clientId);
        } catch (final ApiException apiException) {
            outputFormatter.printApiException("Failed to get client behavior state", apiException);
            return false;
        }

        outputFormatter.printJson(clientStateList);

        return true;
    }
}
