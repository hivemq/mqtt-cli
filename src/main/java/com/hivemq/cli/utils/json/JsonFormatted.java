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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

abstract class JsonFormatted {

    private static final @NotNull Gson gson =
            new GsonBuilder().registerTypeAdapter(Mqtt5UserProperties.class, new Mqtt5UserPropertySerializer())
                    .setPrettyPrinting()
                    .setLenient()
                    .create();

    public @NotNull String toString() {
        return gson.toJson(this);
    }

    private static class Mqtt5UserPropertySerializer implements JsonSerializer<Mqtt5UserProperties> {

        @Override
        public @NotNull JsonElement serialize(
                final @NotNull Mqtt5UserProperties src,
                final @NotNull Type typeOfSrc,
                final @NotNull JsonSerializationContext context) {
            final JsonArray userPropertiesArray = new JsonArray();
            src.asList().forEach(mqtt5UserProperty -> {
                final JsonObject userPropertyObject = new JsonObject();
                userPropertyObject.addProperty("name", mqtt5UserProperty.getName().toString());
                userPropertyObject.addProperty("value", mqtt5UserProperty.getValue().toString());
                userPropertiesArray.add(userPropertyObject);
            });
            return userPropertiesArray;
        }
    }
}
