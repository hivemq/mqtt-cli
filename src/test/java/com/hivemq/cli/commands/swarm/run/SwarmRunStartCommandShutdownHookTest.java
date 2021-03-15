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

/**
 * @author Yannick Weber
 */
class SwarmRunStartCommandShutdownHookTest {

    private @NotNull RunsApi runsApi;
    private @NotNull ScenariosApi scenariosApi;
    private @NotNull SwarmApiErrorTransformer errorTransformer;
    private @NotNull SwarmRunStartCommandShutdownHook shutdownHook;

    @BeforeEach
    void setUp() {
        runsApi = mock(RunsApi.class);
        scenariosApi = mock(ScenariosApi.class);
        errorTransformer = mock(SwarmApiErrorTransformer.class);

        shutdownHook = new SwarmRunStartCommandShutdownHook(runsApi, scenariosApi, errorTransformer, 1, 2);
    }

    @Test
    void stopRunFailed_errorLogged() throws ApiException, InterruptedException {
        final ApiException exception = mock(ApiException.class);
        when(errorTransformer.transformError(any())).thenReturn(new Error(""));
        when(runsApi.stopRun(eq("1"), any())).thenThrow(exception);
        shutdownHook.start();
        shutdownHook.join();
        verify(errorTransformer, times(1)).transformError(any());
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