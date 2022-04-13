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
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SwarmRunStartCommandTest {

    private @NotNull RunsApi runsApi;
    private @NotNull ScenariosApi scenariosApi;
    private @NotNull SwarmApiErrorTransformer swarmApiErrorTransformer;
    private @NotNull ApiClient apiClient;

    @BeforeEach
    void setUp() {
        runsApi = mock(RunsApi.class);
        scenariosApi = mock(ScenariosApi.class);
        swarmApiErrorTransformer = mock(SwarmApiErrorTransformer.class);

        apiClient = mock(ApiClient.class);
        when(runsApi.getApiClient()).thenReturn(apiClient);
        when(scenariosApi.getApiClient()).thenReturn(apiClient);
    }

    @Test
    void invalidUrl_error() {
        final File scenario = mock(File.class);
        final SwarmRunStartCommand command = new SwarmRunStartCommand("invalid",
                scenario,
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        assertEquals(-1, command.call());
        verify(apiClient, times(0)).setBasePath(any());
    }

    @Test
    void fileNull_error() {
        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                null,
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
    }

    @Test
    void scenarioUnreadable_error() {
        final File scenario = mock(File.class);
        when(scenario.canRead()).thenReturn(false);
        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario,
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
    }

    @Test
    void scenarioNotExist_error() {
        final File scenario = mock(File.class);
        when(scenario.canRead()).thenReturn(true);
        when(scenario.exists()).thenReturn(false);
        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario,
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
    }

    @Test
    void uploadScenario_xml_error(final @TempDir @NotNull Path tempDir) throws IOException, ApiException {
        final Path scenario = tempDir.resolve("scenario.xml");
        Files.write(scenario, "scenario-content".getBytes(StandardCharsets.UTF_8));

        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario.toFile(),
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        final ArgumentCaptor<UploadScenarioRequest> upload = ArgumentCaptor.forClass(UploadScenarioRequest.class);

        final ApiException exception = mock(ApiException.class);
        when(swarmApiErrorTransformer.transformError(any())).thenReturn(new Error(""));
        when(scenariosApi.uploadScenario(upload.capture())).thenThrow(exception);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
        verify(scenariosApi).uploadScenario(any());
        verify(swarmApiErrorTransformer).transformError(any());

        final UploadScenarioRequest value = upload.getValue();
        assertNotNull(value.getScenarioName());
        assertTrue(value.getScenarioName().endsWith("scenario"));
        assertEquals("XML", value.getScenarioType());
        assertNull(value.getScenarioDescription());
        assertEquals("c2NlbmFyaW8tY29udGVudA==", value.getScenario());
    }

    @Test
    void uploadScenario_vm_error(final @TempDir @NotNull Path tempDir) throws IOException, ApiException {
        final Path scenario = tempDir.resolve("scenario.vm");
        Files.write(scenario, "scenario-content".getBytes(StandardCharsets.UTF_8));

        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario.toFile(),
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);

        final ArgumentCaptor<UploadScenarioRequest> upload = ArgumentCaptor.forClass(UploadScenarioRequest.class);

        final ApiException exception = mock(ApiException.class);
        when(swarmApiErrorTransformer.transformError(any())).thenReturn(new Error(""));
        when(scenariosApi.uploadScenario(upload.capture())).thenThrow(exception);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
        verify(scenariosApi).uploadScenario(any());
        verify(swarmApiErrorTransformer).transformError(any());

        final UploadScenarioRequest value = upload.getValue();
        assertNotNull(value.getScenarioName());
        assertTrue(value.getScenarioName().endsWith("scenario"));
        assertEquals("VM", value.getScenarioType());
        assertNull(value.getScenarioDescription());
        assertEquals("c2NlbmFyaW8tY29udGVudA==", value.getScenario());
    }

    @Test
    void uploadScenarioSuccessButCantStart_error(final @TempDir @NotNull Path tempDir)
            throws IOException, ApiException {
        final Path scenario = tempDir.resolve("scenario.vm");
        Files.write(scenario, "scenario-content".getBytes(StandardCharsets.UTF_8));

        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario.toFile(),
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);


        final UploadScenarioResponse uploadScenarioResponse = mock(UploadScenarioResponse.class);
        when(scenariosApi.uploadScenario(any())).thenReturn(uploadScenarioResponse);
        when(uploadScenarioResponse.getScenarioId()).thenReturn(1);

        final ApiException exception = mock(ApiException.class);
        when(swarmApiErrorTransformer.transformError(any())).thenReturn(new Error(""));
        when(runsApi.startRun(any())).thenThrow(exception);

        assertEquals(-1, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
        verify(scenariosApi).uploadScenario(any());
        verify(swarmApiErrorTransformer).transformError(any());
        verify(runsApi).startRun(any());
    }

    @Test
    void runScenarioAttached(final @TempDir @NotNull Path tempDir) throws IOException, ApiException {
        final Path scenario = tempDir.resolve("scenario.vm");
        Files.write(scenario, "scenario-content".getBytes(StandardCharsets.UTF_8));

        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario.toFile(),
                false,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);


        final UploadScenarioResponse uploadScenarioResponse = mock(UploadScenarioResponse.class);
        when(scenariosApi.uploadScenario(any())).thenReturn(uploadScenarioResponse);
        when(uploadScenarioResponse.getScenarioId()).thenReturn(42);

        final ArgumentCaptor<StartRunRequest> captor = ArgumentCaptor.forClass(StartRunRequest.class);

        final StartRunResponse startRunResponse = mock(StartRunResponse.class);
        when(startRunResponse.getRunId()).thenReturn(1337);
        when(runsApi.startRun(captor.capture())).thenReturn(startRunResponse);

        final RunResponse runResponse = mock(RunResponse.class);
        when(runResponse.getRunStatus()).thenReturn("FINISHED");
        when(runsApi.getRun("1337")).thenReturn(runResponse);

        assertEquals(0, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
        verify(scenariosApi).uploadScenario(any());
        verify(runsApi).startRun(any());
        verify(runsApi).getRun("1337");

        assertEquals("42", captor.getValue().getScenarioId());
    }

    @Test
    void runScenarioDetached(final @TempDir @NotNull Path tempDir) throws IOException, ApiException {
        final Path scenario = tempDir.resolve("scenario.vm");
        Files.write(scenario, "scenario-content".getBytes(StandardCharsets.UTF_8));

        final SwarmRunStartCommand command = new SwarmRunStartCommand("http://localhost:8080",
                scenario.toFile(),
                true,
                runsApi,
                scenariosApi,
                swarmApiErrorTransformer,
                System.out);


        final UploadScenarioResponse uploadScenarioResponse = mock(UploadScenarioResponse.class);
        when(scenariosApi.uploadScenario(any())).thenReturn(uploadScenarioResponse);
        when(uploadScenarioResponse.getScenarioId()).thenReturn(42);

        final ArgumentCaptor<StartRunRequest> captor = ArgumentCaptor.forClass(StartRunRequest.class);

        final StartRunResponse startRunResponse = mock(StartRunResponse.class);
        when(startRunResponse.getRunId()).thenReturn(1337);
        when(runsApi.startRun(captor.capture())).thenReturn(startRunResponse);

        final RunResponse runResponse = mock(RunResponse.class);
        when(runResponse.getRunStatus()).thenReturn("FINISHED");
        when(runsApi.getRun("1337")).thenReturn(runResponse);

        assertEquals(0, command.call());
        verify(apiClient, times(2)).setBasePath("http://localhost:8080");
        verify(scenariosApi).uploadScenario(any());
        verify(runsApi).startRun(any());
        verify(runsApi, times(0)).getRun("1337");

        assertEquals("42", captor.getValue().getScenarioId());
    }
}