package com.hivemq.cli.commands.swarm.commander;

import com.google.gson.Gson;
import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.CommanderStateResponse;
import com.hivemq.cli.openapi.swarm.RunResponse;
import com.hivemq.cli.openapi.swarm.RunsApi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Yannick Weber
 */
class SwarmStatusCommandTest {

    private @NotNull RunsApi runsApi;
    private @NotNull CommanderApi commanderApi;
    private @NotNull SwarmStatusCommand swarmStatusCommand;
    private @NotNull SwarmApiErrorTransformer errorTransformer;

    @BeforeEach
    void setUp() {
        final Gson gson = new Gson();
        runsApi = mock(RunsApi.class);
        commanderApi = mock(CommanderApi.class);
        errorTransformer = mock(SwarmApiErrorTransformer.class);
        swarmStatusCommand = new SwarmStatusCommand(gson, runsApi, commanderApi, errorTransformer);
    }

    @Test
    void invalidUrl_error() {
    }

    @Test
    void statusNull_error() throws Exception {
        final CommanderStateResponse commanderStateResponse = mock(CommanderStateResponse.class);
        when(commanderStateResponse.getCommanderStatus()).thenReturn(null);
        when(commanderApi.getCommanderStatus()).thenReturn(commanderStateResponse);
        assertEquals(-1, swarmStatusCommand.call());
        verify(commanderApi).getCommanderStatus();
    }

    @Test
    void withRun_requestRun() throws Exception {

        final CommanderStateResponse commanderStateResponse = mock(CommanderStateResponse.class);
        when(commanderStateResponse.getCommanderStatus()).thenReturn("RUNNING");
        when(commanderStateResponse.getRunId()).thenReturn("1");

        when(commanderApi.getCommanderStatus()).thenReturn(commanderStateResponse);

        final RunResponse runResponse = mock(RunResponse.class);
        when(runsApi.getRun("1")).thenReturn(runResponse);

        assertEquals(0, swarmStatusCommand.call());

        verify(commanderApi).getCommanderStatus();
        verify(runsApi).getRun("1");

    }

    @Test
    void withRun_requestRunException_error() throws Exception {

        final CommanderStateResponse commanderStateResponse = mock(CommanderStateResponse.class);
        when(commanderStateResponse.getCommanderStatus()).thenReturn("RUNNING");
        when(commanderStateResponse.getRunId()).thenReturn("1");

        when(commanderApi.getCommanderStatus()).thenReturn(commanderStateResponse);

        final ApiException exception = mock(ApiException.class);
        when(exception.getResponseBody()).thenReturn("error");
        when(errorTransformer.transformError(any())).thenReturn(new Error(""));
        when(runsApi.getRun("1")).thenThrow(exception);

        assertEquals(-1, swarmStatusCommand.call());

        verify(commanderApi).getCommanderStatus();
        verify(runsApi).getRun("1");
    }

    @Test
    void withoutRun_notRequestRun() throws Exception {

        final CommanderStateResponse commanderStateResponse = mock(CommanderStateResponse.class);
        when(commanderStateResponse.getCommanderStatus()).thenReturn("RUNNING");
        when(commanderStateResponse.getRunId()).thenReturn(null);

        when(commanderApi.getCommanderStatus()).thenReturn(commanderStateResponse);

        assertEquals(0, swarmStatusCommand.call());

        verify(commanderApi).getCommanderStatus();
        verify(runsApi, times(0)).getRun("1");

    }

    @Test
    void commanderStatusError_error() throws ApiException {
        final ApiException exception = mock(ApiException.class);
        when(exception.getResponseBody()).thenReturn("error");
        when(errorTransformer.transformError(any())).thenReturn(new Error(""));
        when(commanderApi.getCommanderStatus()).thenThrow(exception);
        assertEquals(-1, swarmStatusCommand.call());
    }
}