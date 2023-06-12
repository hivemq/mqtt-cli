package com.hivemq.cli.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.Schema;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaSerializerTest {

    private final @NotNull Gson gson = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
            .registerTypeAdapter(Schema.class, new SchemaSerializer())
            .create();
    private final @NotNull JSON openapiSerialization = new JSON();

    @Test
    void serialize_allFields_jsonIdentical() {
        final String schemaJson = "{\"id\":\"protobuf-1-schema\",\"version\":1,\"createdAt\":\"2023-01-01T01:02:03.004Z\"," +
                "\"type\":\"schemaType\",\"schemaDefinition\":\"abc\",\"arguments\":{\"argB\":\"valB\",\"argA\":\"valA\"}}";

        final Schema schema = openapiSerialization.deserialize(schemaJson, Schema.class);
        final String serialized = gson.toJson(schema);
        assertEquals(schemaJson, serialized);
    }
}
