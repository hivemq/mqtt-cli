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
import com.hivemq.cli.openapi.hivemq.DataHubStateApi;
import com.hivemq.cli.openapi.hivemq.FsmStatesInformationListItem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetBehaviorStateTaskTest {

    private final @NotNull DataHubStateApi stateApi = mock(DataHubStateApi.class);
    private final @NotNull OutputFormatter outputFormatter = mock(OutputFormatter.class);

    private static final @NotNull String CLIENT_ID = "client-1";

    @Test
    void execute_validId_success() throws ApiException {
        final FsmStatesInformationListItem statesInformation = new FsmStatesInformationListItem();

        final GetBehaviorStateTask task = new GetBehaviorStateTask(outputFormatter, stateApi, CLIENT_ID);

        when(stateApi.getClientState(eq(CLIENT_ID))).thenReturn(statesInformation);

        assertTrue(task.execute());
        verify(stateApi, times(1)).getClientState(eq(CLIENT_ID));
        verify(outputFormatter, times(1)).printJson(statesInformation);
    }

    @Test
    void execute_apiException_printError() throws ApiException {
        final GetBehaviorStateTask task = new GetBehaviorStateTask(outputFormatter, stateApi, CLIENT_ID);

        when(stateApi.getClientState(any())).thenThrow(ApiException.class);

        assertFalse(task.execute());
        verify(outputFormatter, times(1)).printApiException(any(), any());
    }

}
