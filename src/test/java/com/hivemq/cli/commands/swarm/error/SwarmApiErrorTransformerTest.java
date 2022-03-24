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
package com.hivemq.cli.commands.swarm.error;

import com.google.gson.Gson;
import com.hivemq.cli.openapi.ApiException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(apiException.getCode()).thenReturn(400);
        when(apiException.getResponseBody()).thenReturn("");
        final Error error = swarmApiErrorTransformer.transformError(apiException);

        assertEquals("Unspecified Error.", error.getDetail());
    }

    @Test
    void transformValidError_returned() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getCode()).thenReturn(400);
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

    @Test
    void transform0_connectionRefused() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getCode()).thenReturn(0);
        final Error error = swarmApiErrorTransformer.transformError(apiException);

        assertEquals("Connection Refused. Check if the Commander REST-endpoint is enabled.", error.getDetail());

    }

    @Test
    void transform404_notFound() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getCode()).thenReturn(404);
        final Error error = swarmApiErrorTransformer.transformError(apiException);

        assertEquals("Not found. Check if the commander is running in REST-mode.", error.getDetail());
    }

    @Test
    void invalidJson_actualErrorPrinted() {
        final ApiException apiException = mock(ApiException.class);
        when(apiException.getCode()).thenReturn(400);
        when(apiException.getResponseBody()).thenReturn("invalid json");
        final Error error = swarmApiErrorTransformer.transformError(apiException);

        assertEquals("invalid json", error.getDetail());
    }
}