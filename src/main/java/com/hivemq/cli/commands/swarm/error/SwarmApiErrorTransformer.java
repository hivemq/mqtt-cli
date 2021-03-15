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
