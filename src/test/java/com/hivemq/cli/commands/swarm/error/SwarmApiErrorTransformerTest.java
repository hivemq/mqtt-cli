package com.hivemq.cli.commands.swarm.error;

import com.google.gson.Gson;
import com.hivemq.cli.openapi.ApiException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Yannick Weber
 */
class SwarmApiErrorTransformerTest {

    private @NotNull SwarmApiErrorTransformer swarmApiErrorTransformer;

    @BeforeEach
    void setUp() {
        final Gson gson = new Gson();
        swarmApiErrorTransformer = new SwarmApiErrorTransformer(gson);
    }

    @Test
    void transformEmptyString_emptyError() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getResponseBody()).thenReturn("");
        final Error error = swarmApiErrorTransformer.transformError(apiException);
        assertEquals("Unspecified Error.", error.getDetail());
    }

    @Test
    void transformValidError_returned() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getResponseBody()).thenReturn(
                "{\"errors\":[{\"title\":\"Requested Resource Not Found.\",\"detail\":\"Run with id '3' was not found.\"}]}");
        final Error error = swarmApiErrorTransformer.transformError(apiException);
        assertEquals("Run with id '3' was not found.", error.getDetail());
    }

    @Test
    void transform500_internalError() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getCode()).thenReturn(500);
        final Error error = swarmApiErrorTransformer.transformError(apiException);
        assertEquals("Internal Server Error.", error.getDetail());
    }
}