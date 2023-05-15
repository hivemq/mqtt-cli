package com.hivemq.cli.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {

    @Override
    public @NotNull JsonElement serialize(
            final @NotNull OffsetDateTime dateTime,
            final @NotNull Type typeOfSrc,
            final @NotNull JsonSerializationContext context) {
        return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(dateTime));
    }

}
