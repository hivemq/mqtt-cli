package com.hivemq.cli.utils;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

public class JsonUtils {

    private final static  Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();

    public static String prettyFormat(final @NotNull String json) {
        try {
            JsonElement je = JsonParser.parseString(json);
            return gson.toJson(je);
        }
        catch (JsonSyntaxException ex) {
            return json;
        }
    }
}
