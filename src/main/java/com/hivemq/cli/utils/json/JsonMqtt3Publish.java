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
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonMqtt3Publish extends JsonFormatted {

    @NotNull private final String topic;
    @NotNull private JsonElement payload;
    @NotNull private MqttQos qos;
    @NotNull private String receivedAt;
    private boolean isRetain;

    public JsonMqtt3Publish(final @NotNull Mqtt3Publish publish, boolean isBase64) {
        String payloadString;
        if (isBase64) {
            payloadString = Base64.toBase64String(publish.getPayloadAsBytes());
        }
        else {
            payloadString = new String(publish.getPayloadAsBytes());
        }

        topic = publish.getTopic().toString();

        try {
            payload = JsonParser.parseString(payloadString);
        }
        catch (JsonSyntaxException ex) {
            payload = new JsonPrimitive(payloadString);
        }

        qos = publish.getQos();
        receivedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        isRetain = publish.isRetain();
    }

}
