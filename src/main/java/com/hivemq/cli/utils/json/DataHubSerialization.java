package com.hivemq.cli.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.hivemq.cli.openapi.hivemq.PolicyOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataHubSerialization {
    public static @Nullable JsonElement serializePolicyOperation(
            final @Nullable PolicyOperation policyOperation, final @NotNull JsonSerializationContext context) {
        if (policyOperation == null) {
            return null;
        }

        final JsonObject operationObject = new JsonObject();
        operationObject.add(PolicyOperation.SERIALIZED_NAME_ID, context.serialize(policyOperation.getId()));
        operationObject.add(PolicyOperation.SERIALIZED_NAME_FUNCTION_ID,
                context.serialize(policyOperation.getFunctionId()));
        operationObject.add(PolicyOperation.SERIALIZED_NAME_ARGUMENTS,
                context.serialize(policyOperation.getArguments()));
        return operationObject;
    }
}
