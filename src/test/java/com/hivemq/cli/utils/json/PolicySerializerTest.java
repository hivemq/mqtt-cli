package com.hivemq.cli.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.Policy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolicySerializerTest {

    private final @NotNull Gson gson = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
            .registerTypeAdapter(Policy.class, new PolicySerializer())
            .create();
    private final @NotNull JSON openapiSerialization = new JSON();

    @Test
    void serialize_allFields_jsonIdentical() {
        final String policyJson = "{\"id\":\"policy-id\"," +
                "\"createdAt\":\"2023-01-01T01:02:03.004Z\"," +
                "\"matching\":{\"topicFilter\":\"filter\"}," +
                "\"validation\":{\"validators\":[{\"type\":\"schema\",\"arguments\":{\"strategy\":\"ALL_OF\",\"schemas\":[{\"schemaId\":\"schema-id\",\"version\":\"latest\"}]}}]}," +
                "\"onSuccess\":{\"pipeline\":[{\"id\":\"success\",\"functionId\":\"successFunction\",\"arguments\":{\"argB\":\"valB\",\"argA\":\"valA\"}}]}," +
                "\"onFailure\":{\"pipeline\":[{\"id\":\"failure\",\"functionId\":\"failureFunction\",\"arguments\":{\"argA\":\"valA\",\"argB\":\"valB\"}}]}}";

        final Policy policy = openapiSerialization.deserialize(policyJson, Policy.class);
        final String serialized = gson.toJson(policy);
        assertEquals(policyJson, serialized);
    }

    @Test
    void serialize_minimalFields_jsonIdentical() {
        final String policyJson = "{\"id\":\"policy-id\"," + "\"createdAt\":\"2023-01-01T01:02:03.004Z\"}";

        final Policy policy = openapiSerialization.deserialize(policyJson, Policy.class);
        final String serialized = gson.toJson(policy);
        assertEquals(policyJson, serialized);
    }
}
