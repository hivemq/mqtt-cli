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

package com.hivemq.cli.commands.hivemq.datahub;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.openapi.ApiException;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import javax.inject.Inject;
import java.io.PrintStream;

public class OutputFormatter {

    private final @NotNull Gson gson;
    private final @NotNull PrintStream out;

    @Inject
    public OutputFormatter(final @NotNull PrintStream out, final @NotNull Gson gson) {
        this.out = out;
        this.gson = gson;
    }

    public void printJson(final @NotNull Object object) {
        out.println(gson.toJson(object));
    }

    public void printError(final @NotNull String message) {
        System.err.println(message);
        Logger.error(message);
    }

    public void printApiException(
            final @NotNull String operationDescription,
            final @NotNull ApiException apiException) {
        final String response = apiException.getResponseBody();

        String prettyResponse;
        if (response == null) {
            prettyResponse = apiException.getMessage();
        } else {
            try {
                final JsonObject object = JsonParser.parseString(response).getAsJsonObject();
                prettyResponse = gson.toJson(object);
            } catch (final JsonSyntaxException jsonSyntaxException) {
                prettyResponse = response;
            }
        }
        System.err.println(prettyResponse);
        Logger.error("{}: {}", operationDescription, prettyResponse);
    }
}
