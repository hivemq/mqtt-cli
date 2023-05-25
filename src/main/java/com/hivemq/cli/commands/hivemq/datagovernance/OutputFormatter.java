package com.hivemq.cli.commands.hivemq.datagovernance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.utils.json.OffsetDateTimeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import javax.inject.Inject;
import java.io.PrintStream;
import java.time.OffsetDateTime;

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

    public void printApiException(final @NotNull String operationName, final @NotNull ApiException apiException) {
        final @Nullable String response = apiException.getResponseBody();

        String prettyResponse;
        try {
            if (response == null) {
                prettyResponse = apiException.getMessage();
            } else {
                final JsonObject object = JsonParser.parseString(response).getAsJsonObject();
                prettyResponse = gson.toJson(object);
            }
        } catch (final JsonSyntaxException jsonSyntaxException) {
            prettyResponse = response;
        }
        System.err.println(prettyResponse);
        Logger.error("{}: {}", operationName, prettyResponse);
    }
}
