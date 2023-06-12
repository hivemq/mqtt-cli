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
import com.hivemq.cli.openapi.hivemq.Schema;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * The generated OpenAPI classes do not preserve JSON field ordering.
 * This serializer manually restores the correct order.
 */
public class SchemaSerializer implements JsonSerializer<Schema> {

    @Override
    public @NotNull JsonElement serialize(
            final @NotNull Schema schema,
            final @NotNull Type typeOfSrc,
            final @NotNull JsonSerializationContext context) {

        final JsonObject object = new JsonObject();
        object.add(Schema.SERIALIZED_NAME_ID,context.serialize(schema.getId()));
        object.add(Schema.SERIALIZED_NAME_VERSION,context.serialize(schema.getVersion()));
        object.add(Schema.SERIALIZED_NAME_CREATED_AT, context.serialize(schema.getCreatedAt()));
        object.add(Schema.SERIALIZED_NAME_TYPE, context.serialize(schema.getType()));
        object.add(Schema.SERIALIZED_NAME_SCHEMA_DEFINITION, context.serialize(schema.getSchemaDefinition()));
        object.add(Schema.SERIALIZED_NAME_ARGUMENTS, context.serialize(schema.getArguments()));

        return object;
    }

}
