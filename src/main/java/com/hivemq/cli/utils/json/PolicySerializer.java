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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hivemq.cli.openapi.hivemq.Policy;
import com.hivemq.cli.openapi.hivemq.PolicyAction;
import com.hivemq.cli.openapi.hivemq.PolicyOperation;
import com.hivemq.cli.openapi.hivemq.PolicyValidation;
import com.hivemq.cli.openapi.hivemq.PolicyValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * The generated OpenAPI classes do not preserve JSON field ordering.
 * This serializer manually restores the correct order.
 */
public class PolicySerializer implements JsonSerializer<Policy> {

    @Override
    public @NotNull JsonElement serialize(
            final @NotNull Policy policy,
            final @NotNull Type typeOfSrc,
            final @NotNull JsonSerializationContext context) {

        final JsonObject object = new JsonObject();
        object.add("id", context.serialize(policy.getId()));
        object.add("createdAt", context.serialize(policy.getCreatedAt()));
        object.add("matching", context.serialize(policy.getMatching()));
        object.add("validation", serializeValidation(policy.getValidation(), context));
        object.add("onSuccess", serializePolicyAction(policy.getOnSuccess(), context));
        object.add("onFailure", serializePolicyAction(policy.getOnFailure(), context));

        return object;
    }

    private @Nullable JsonElement serializeValidation(
            final @Nullable PolicyValidation policyValidation, final @NotNull JsonSerializationContext context) {
        if (policyValidation == null) {
            return null;
        }

        final JsonObject object = new JsonObject();

        if (policyValidation.getValidators() == null) {
            return object;
        }

        final JsonArray validatorsArray = new JsonArray();

        for (final PolicyValidator validator : policyValidation.getValidators()) {
            final JsonObject validatorObject = new JsonObject();
            validatorObject.add("type", context.serialize(validator.getType()));
            validatorObject.add("arguments", context.serialize(validator.getArguments()));
            validatorsArray.add(validatorObject);
        }

        object.add("validators", context.serialize(validatorsArray));
        return object;
    }

    private @Nullable JsonElement serializePolicyAction(
            final @Nullable PolicyAction policyAction, final @NotNull JsonSerializationContext context) {
        if (policyAction == null) {
            return null;
        }

        final JsonObject object = new JsonObject();

        if (policyAction.getPipeline() == null) {
            return object;
        }

        final JsonArray operationsArray = new JsonArray();

        for (final PolicyOperation operation : policyAction.getPipeline()) {
            final JsonObject operationObject = new JsonObject();
            operationObject.add("id", context.serialize(operation.getId()));
            operationObject.add("functionId", context.serialize(operation.getFunctionId()));
            operationObject.add("arguments", context.serialize(operation.getArguments()));
            operationsArray.add(operationObject);
        }

        object.add("pipeline", context.serialize(operationsArray));
        return object;
    }

}
