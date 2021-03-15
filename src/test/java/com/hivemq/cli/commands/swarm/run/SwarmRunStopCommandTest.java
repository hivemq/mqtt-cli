/**
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.RunsApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Yannick Weber
 */
class SwarmRunStopCommandTest {

    private @NotNull RunsApi runsApi;
    private @NotNull SwarmApiErrorTransformer errorTransformer;
    private @NotNull ApiClient apiClient;

    @BeforeEach
    void setUp() {
        runsApi = mock(RunsApi.class);
        errorTransformer = mock(SwarmApiErrorTransformer.class);

        apiClient = mock(ApiClient.class);
        when(runsApi.getApiClient()).thenReturn(apiClient);

        final Error error = mock(Error.class);
        when(errorTransformer.transformError(any())).thenReturn(error);
    }

    @Test
    void invalidUrl_error() throws ApiException {
        final SwarmRunStopCommand invalid = new SwarmRunStopCommand("invalid", 1, runsApi, errorTransformer);
        assertEquals(-1, invalid.call());
        verify(runsApi, times(0)).getRun(any());
        verify(apiClient, times(0)).setBasePath(anyString());
    }

    @Test
    void exception_error() throws ApiException {
        final SwarmRunStopCommand invalid = new SwarmRunStopCommand("http://localhost:8080", 1, runsApi, errorTransformer);

        when(runsApi.stopRun(any(), any())).thenThrow(mock(ApiException.class));

        assertEquals(-1, invalid.call());
        verify(runsApi).stopRun(eq("1"), any());
        verify(apiClient).setBasePath("http://localhost:8080");
        verify(errorTransformer).transformError(any());
    }

    @Test
    void noException_success() throws ApiException {
        final SwarmRunStopCommand invalid = new SwarmRunStopCommand("http://localhost:8080", 1, runsApi, errorTransformer);

        assertEquals(0, invalid.call());
        verify(runsApi).stopRun(eq("1"), any());
        verify(apiClient).setBasePath("http://localhost:8080");
        verify(errorTransformer, times(0)).transformError(any());
    }
}