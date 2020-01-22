/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonMqttPublish extends JsonFormatted {

    @NotNull private final String topic;
    @NotNull private JsonElement payload;
    @NotNull private MqttQos qos;
    @NotNull private String receivedAt;
    private boolean retain;
    @Nullable private String contentType;
    @Nullable private Mqtt5PayloadFormatIndicator payloadFormatIndicator;
    @Nullable private Long messageExpiryInterval;
    @Nullable private String responseTopic;
    @Nullable private String correlationData;
    @Nullable private Map<String, String> userProperties;

    public JsonMqttPublish(final @NotNull Mqtt3Publish publish, final boolean isBase64) {
        payload = payloadToJson(publish.getPayloadAsBytes(), isBase64);
        topic = publish.getTopic().toString();
        qos = publish.getQos();
        receivedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        retain = publish.isRetain();
    }

    public JsonMqttPublish(final @NotNull Mqtt5Publish publish, final boolean isBase64) {
        payload = payloadToJson(publish.getPayloadAsBytes(), isBase64);
        topic = publish.getTopic().toString();
        qos = publish.getQos();
        receivedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        retain = publish.isRetain();
        contentType = publish.getContentType().map(MqttUtf8String::toString).orElse(null);
        payloadFormatIndicator = publish.getPayloadFormatIndicator().orElse(null);

        if (publish.getMessageExpiryInterval().isPresent()) {
            messageExpiryInterval = publish.getMessageExpiryInterval().getAsLong();
        }

        responseTopic = publish.getResponseTopic().map(MqttTopic::toString).orElse(null);
        correlationData = publish.getCorrelationData()
                .map(cd -> new String(cd.array(), StandardCharsets.UTF_8))
                .orElse(null);

        if (publish.getUserProperties().asList().size() > 0) {
            userProperties = new HashMap<>();
            publish.getUserProperties().asList()
                    .forEach(up ->  userProperties.put(up.getName().toString(), up.getValue().toString()));
        }
    }

    private JsonElement payloadToJson(final byte[] payload, final boolean isBase64) {
        final String payloadString;
        if (isBase64) { payloadString = Base64.toBase64String(payload); }
        else { payloadString = new String(payload); }

        JsonElement payloadJson;
        try { payloadJson = JsonParser.parseString(payloadString); }
        catch (JsonSyntaxException ex) { payloadJson = new JsonPrimitive(payloadString); }

        return payloadJson;
    }
}
