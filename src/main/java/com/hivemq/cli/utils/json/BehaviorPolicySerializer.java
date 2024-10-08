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
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiBehaviorPolicy;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiBehaviorPolicyBehavior;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiBehaviorPolicyOnEvent;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiBehaviorPolicyOnTransition;
import com.hivemq.cli.openapi.hivemq.HivemqOpenapiPolicyOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

import static com.hivemq.cli.utils.json.DataHubSerialization.serializePolicyOperation;

/**
 * The generated OpenAPI classes do not preserve JSON field ordering.
 * This serializer manually restores the correct order.
 */
public class BehaviorPolicySerializer implements JsonSerializer<HivemqOpenapiBehaviorPolicy> {

    @Override
    public @NotNull JsonElement serialize(
            final @NotNull HivemqOpenapiBehaviorPolicy policy,
            final @NotNull Type typeOfSrc,
            final @NotNull JsonSerializationContext context) {

        final JsonObject object = new JsonObject();
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_ID, context.serialize(policy.getId()));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_CREATED_AT, context.serialize(policy.getCreatedAt()));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_LAST_UPDATED_AT,
                context.serialize(policy.getLastUpdatedAt()));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_MATCHING, context.serialize(policy.getMatching()));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_DESERIALIZATION,
                context.serialize(policy.getDeserialization()));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_BEHAVIOR,
                serializePolicyBehavior(policy.getBehavior(), context));
        object.add(HivemqOpenapiBehaviorPolicy.SERIALIZED_NAME_ON_TRANSITIONS,
                serializeOnTransitions(policy.getOnTransitions(), context));

        return object;
    }

    private @Nullable JsonElement serializeOnTransitions(
            final @Nullable List<HivemqOpenapiBehaviorPolicyOnTransition> onTransitions,
            final @NotNull JsonSerializationContext context) {
        if (onTransitions == null) {
            return null;
        }

        final JsonArray arrayObject = new JsonArray();

        for (final HivemqOpenapiBehaviorPolicyOnTransition onTransition : onTransitions) {
            final JsonObject onTransitionObject = new JsonObject();
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_FROM_STATE,
                    context.serialize(onTransition.getFromState()));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_TO_STATE,
                    context.serialize(onTransition.getToState()));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_EVENT_ON_ANY,
                    serializeBehaviorPolicyOnEvent(onTransition.getEventOnAny(), context));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_MQTT_ON_INBOUND_CONNECT,
                    serializeBehaviorPolicyOnEvent(onTransition.getMqttOnInboundConnect(), context));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_MQTT_ON_INBOUND_PUBLISH,
                    serializeBehaviorPolicyOnEvent(onTransition.getMqttOnInboundPublish(), context));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_MQTT_ON_INBOUND_SUBSCRIBE,
                    serializeBehaviorPolicyOnEvent(onTransition.getMqttOnInboundSubscribe(), context));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_MQTT_ON_INBOUND_DISCONNECT,
                    serializeBehaviorPolicyOnEvent(onTransition.getMqttOnInboundDisconnect(), context));
            onTransitionObject.add(HivemqOpenapiBehaviorPolicyOnTransition.SERIALIZED_NAME_CONNECTION_ON_DISCONNECT,
                    serializeBehaviorPolicyOnEvent(onTransition.getConnectionOnDisconnect(), context));
            arrayObject.add(onTransitionObject);
        }

        return arrayObject;
    }

    private @Nullable JsonElement serializeBehaviorPolicyOnEvent(
            final @Nullable HivemqOpenapiBehaviorPolicyOnEvent onEvent,
            final @NotNull JsonSerializationContext context) {
        if (onEvent == null) {
            return null;
        }

        final JsonObject object = new JsonObject();
        if (onEvent.getPipeline() == null) {
            return object;
        }

        final JsonArray operationsArray = new JsonArray();
        for (final HivemqOpenapiPolicyOperation operation : onEvent.getPipeline()) {
            operationsArray.add(serializePolicyOperation(operation, context));
        }

        object.add(HivemqOpenapiBehaviorPolicyOnEvent.SERIALIZED_NAME_PIPELINE, context.serialize(operationsArray));
        return object;
    }

    private @Nullable JsonElement serializePolicyBehavior(
            final @Nullable HivemqOpenapiBehaviorPolicyBehavior policyBehavior,
            final @NotNull JsonSerializationContext context) {
        if (policyBehavior == null) {
            return null;
        }

        final JsonObject object = new JsonObject();
        object.add(HivemqOpenapiBehaviorPolicyBehavior.SERIALIZED_NAME_ID, context.serialize(policyBehavior.getId()));
        object.add(HivemqOpenapiBehaviorPolicyBehavior.SERIALIZED_NAME_ARGUMENTS,
                context.serialize(policyBehavior.getArguments()));

        return object;
    }

}
