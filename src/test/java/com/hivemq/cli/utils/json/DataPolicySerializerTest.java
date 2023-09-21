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
package com.hivemq.cli.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.openapi.JSON;
import com.hivemq.cli.openapi.hivemq.DataPolicy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataPolicySerializerTest {

    private final @NotNull Gson gson = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
            .registerTypeAdapter(DataPolicy.class, new DataPolicySerializer())
            .create();
    private final @NotNull JSON openapiSerialization = new JSON();

    @Test
    void serialize_allFields_jsonIdentical() {
        final String dataPolicyJson = "{\"id\":\"policy-id\"," +
                "\"createdAt\":\"2023-01-01T01:02:03.004Z\"," +
                "\"matching\":{\"topicFilter\":\"filter\"}," +
                "\"validation\":{\"validators\":[{\"type\":\"schema\",\"arguments\":{\"strategy\":\"ALL_OF\",\"schemas\":[{\"schemaId\":\"schema-id\",\"version\":\"latest\"}]}}]}," +
                "\"onSuccess\":{\"pipeline\":[{\"id\":\"success\",\"functionId\":\"successFunction\",\"arguments\":{\"argB\":\"valB\",\"argA\":\"valA\"}}]}," +
                "\"onFailure\":{\"pipeline\":[{\"id\":\"failure\",\"functionId\":\"failureFunction\",\"arguments\":{\"argA\":\"valA\",\"argB\":\"valB\"}}]}}";

        final DataPolicy policy = openapiSerialization.deserialize(dataPolicyJson, DataPolicy.class);
        final String serialized = gson.toJson(policy);
        assertEquals(dataPolicyJson, serialized);
    }

    @Test
    void serialize_minimalFields_jsonIdentical() {
        final String policyJson = "{\"id\":\"policy-id\"," + "\"createdAt\":\"2023-01-01T01:02:03.004Z\"}";

        final DataPolicy policy = openapiSerialization.deserialize(policyJson, DataPolicy.class);
        final String serialized = gson.toJson(policy);
        assertEquals(policyJson, serialized);
    }
}
