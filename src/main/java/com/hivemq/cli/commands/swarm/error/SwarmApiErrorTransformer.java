package com.hivemq.cli.commands.swarm.error;

import com.google.gson.Gson;
import com.hivemq.cli.openapi.ApiException;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * @author Yannick Weber
 */
public class SwarmApiErrorTransformer {

    private final @NotNull Gson gson;

    @Inject
    public SwarmApiErrorTransformer(final @NotNull Gson gson) {
        this.gson = gson;
    }

    public @NotNull Error transformError(final @NotNull ApiException apiException) {
        if (apiException.getCode() == 500) {
            return new Error("Internal Server Error.");
        }
        final String body = apiException.getResponseBody();
        if (body == null || body.isEmpty()) {
            return new Error("Unspecified Error.");
        }
        final ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        return errorResponse.getErrors().get(0);
    }

}
