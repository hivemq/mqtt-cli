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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hivemq.cli.openapi.hivemq.Script;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * The generated OpenAPI classes do not preserve JSON field ordering.
 * This serializer manually restores the correct order.
 */
public class ScriptSerializer implements JsonSerializer<Script> {

    @Override
    public @NotNull JsonElement serialize(
            final @NotNull Script script,
            final @NotNull Type typeOfSrc,
            final @NotNull JsonSerializationContext context) {

        final JsonObject object = new JsonObject();
        object.add(Script.SERIALIZED_NAME_ID,context.serialize(script.getId()));
        object.add(Script.SERIALIZED_NAME_VERSION,context.serialize(script.getVersion()));
        object.add(Script.SERIALIZED_NAME_CREATED_AT, context.serialize(script.getCreatedAt()));
        object.add(Script.SERIALIZED_NAME_FUNCTION_TYPE, context.serialize(script.getFunctionType()));
        object.add(Script.SERIALIZED_NAME_DESCRIPTION, context.serialize(script.getDescription()));
        object.add(Script.SERIALIZED_NAME_SOURCE, context.serialize(script.getSource()));

        return object;
    }

}
