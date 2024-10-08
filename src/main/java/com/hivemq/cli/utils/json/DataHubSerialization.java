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
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiPolicyOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DataHubSerialization {
    public static @Nullable JsonElement serializePolicyOperation(
            final @Nullable HivemqOpenapiPolicyOperation policyOperation,
            final @NotNull JsonSerializationContext context) {
        if (policyOperation == null) {
            return null;
        }

        final JsonObject operationObject = new JsonObject();
        operationObject.add(HivemqOpenapiPolicyOperation.SERIALIZED_NAME_ID,
                context.serialize(policyOperation.getId()));
        operationObject.add(HivemqOpenapiPolicyOperation.SERIALIZED_NAME_FUNCTION_ID,
                context.serialize(policyOperation.getFunctionId()));
        operationObject.add(HivemqOpenapiPolicyOperation.SERIALIZED_NAME_ARGUMENTS,
                context.serialize(policyOperation.getArguments()));
        operationObject.add(HivemqOpenapiPolicyOperation.SERIALIZED_NAME_ON_BRANCH,
                context.serialize(emptyToNull(policyOperation.getOnBranch())));
        return operationObject;
    }

    private static <T extends Collection<?>> @Nullable T emptyToNull(final @Nullable T collection) {
        return (collection == null) ? null : ((collection.isEmpty()) ? null : collection);
    }
}
