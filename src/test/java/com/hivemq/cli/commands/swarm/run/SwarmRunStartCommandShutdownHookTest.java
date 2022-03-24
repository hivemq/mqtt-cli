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
package com.hivemq.cli.commands.swarm.run;

import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.ScenariosApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SwarmRunStartCommandShutdownHookTest {

    private @NotNull ScenariosApi scenariosApi;
    private @NotNull SwarmApiErrorTransformer errorTransformer;
    private @NotNull SwarmRunStartCommandShutdownHook shutdownHook;

    @BeforeEach
    void setUp() {
        final RunsApi runsApi = mock(RunsApi.class);
        scenariosApi = mock(ScenariosApi.class);
        errorTransformer = mock(SwarmApiErrorTransformer.class);

        shutdownHook = new SwarmRunStartCommandShutdownHook(runsApi, scenariosApi, errorTransformer, 1, 2);
    }

    @Test
    void deleteScenario_errorLogged() throws ApiException, InterruptedException {
        final ApiException exception = mock(ApiException.class);
        when(errorTransformer.transformError(any())).thenReturn(new Error(""));
        doThrow(exception).when(scenariosApi).deleteScenario("2");
        shutdownHook.start();
        shutdownHook.join();
        verify(errorTransformer, times(1)).transformError(any());
    }
}