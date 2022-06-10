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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.hivemq.cli.utils.MqttPublishUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class JsonMqttPublish extends JsonFormatted {

    private final @NotNull String topic;
    private final @NotNull JsonElement payload;
    private final @NotNull MqttQos qos;
    private final @NotNull String receivedAt;
    private final boolean retain;
    private @Nullable String contentType;
    private @Nullable Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    private @Nullable Long messageExpiryInterval;
    private @Nullable String responseTopic;
    private @Nullable String correlationData;
    private @Nullable Map<String, String> userProperties;

    public JsonMqttPublish(final @NotNull Mqtt3Publish publish, final boolean isBase64) {
        payload = payloadToJson(publish.getPayloadAsBytes(), isBase64);
        topic = publish.getTopic().toString();
        qos = publish.getQos();
        receivedAt = getReceivedAt();
        retain = publish.isRetain();
    }

    public JsonMqttPublish(final @NotNull Mqtt5Publish publish, final boolean isBase64) {
        payload = payloadToJson(publish.getPayloadAsBytes(), isBase64);
        topic = publish.getTopic().toString();
        qos = publish.getQos();
        receivedAt = getReceivedAt();
        retain = publish.isRetain();
        contentType = publish.getContentType().map(MqttUtf8String::toString).orElse(null);
        payloadFormatIndicator = publish.getPayloadFormatIndicator().orElse(null);

        if (publish.getMessageExpiryInterval().isPresent()) {
            messageExpiryInterval = publish.getMessageExpiryInterval().getAsLong();
        }

        responseTopic = publish.getResponseTopic().map(MqttTopic::toString).orElse(null);
        correlationData = publish.getCorrelationData() //
                .map(cd -> StandardCharsets.UTF_8.decode(cd).toString()) //
                .orElse(null);

        if (publish.getUserProperties().asList().size() > 0) {
            userProperties = new HashMap<>();
            publish.getUserProperties()
                    .asList()
                    .forEach(up -> Objects.requireNonNull(userProperties)
                            .put(up.getName().toString(), up.getValue().toString()));
        }
    }

    private @NotNull JsonElement payloadToJson(final byte @NotNull [] payload, final boolean isBase64) {
        final String payloadString = MqttPublishUtils.formatPayload(payload, isBase64);

        JsonElement payloadJson;
        try {
            payloadJson = JsonParser.parseString(payloadString);
        } catch (final JsonSyntaxException e) {
            payloadJson = new JsonPrimitive(payloadString);
        }

        return payloadJson;
    }

    private @NotNull String getReceivedAt() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
