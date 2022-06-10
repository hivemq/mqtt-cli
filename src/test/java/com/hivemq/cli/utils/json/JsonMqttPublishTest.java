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
import com.google.gson.reflect.TypeToken;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import static com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator.UNSPECIFIED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonMqttPublishTest {

    public static final @NotNull Type STRING_STRING_MAP = new TypeToken<Map<String, String>>() {}.getType();
    private static final @NotNull Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    public static final byte @NotNull [] BYTES = "hello".getBytes(StandardCharsets.UTF_8);

    @Test
    void mqtt3_base64Payload_converted() {
        final Mqtt3Publish mqtt3Publish = mock(Mqtt3Publish.class);
        when(mqtt3Publish.getPayloadAsBytes()).thenReturn(BYTES);
        when(mqtt3Publish.getTopic()).thenReturn(MqttTopic.of("myTopic"));
        when(mqtt3Publish.getQos()).thenReturn(MqttQos.AT_LEAST_ONCE);
        when(mqtt3Publish.isRetain()).thenReturn(true);

        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(mqtt3Publish, true);
        final Map<String, String> map = GSON.fromJson(jsonMqttPublish.toString(), STRING_STRING_MAP);

        assertEquals(Base64.toBase64String(BYTES), map.get("payload"));
        assertEquals("myTopic", map.get("topic"));
        assertEquals("AT_LEAST_ONCE", map.get("qos"));
        assertEquals(Boolean.toString(true), map.get("retain"));

        assertNull(map.get("contentType"));
        assertNull(map.get("payloadFormatIndicator"));
        assertNull(map.get("messageExpiryInterval"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("correlationData"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("userProperties"));
    }

    @Test
    void mqtt3_utf8Payload_converted() {
        final Mqtt3Publish mqtt3Publish = mock(Mqtt3Publish.class);
        when(mqtt3Publish.getPayloadAsBytes()).thenReturn(BYTES);
        when(mqtt3Publish.getTopic()).thenReturn(MqttTopic.of("myTopic"));
        when(mqtt3Publish.getQos()).thenReturn(MqttQos.AT_LEAST_ONCE);
        when(mqtt3Publish.isRetain()).thenReturn(true);

        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(mqtt3Publish, false);
        final Map<String, String> map = GSON.fromJson(jsonMqttPublish.toString(), STRING_STRING_MAP);

        assertEquals("hello", map.get("payload"));
        assertEquals("myTopic", map.get("topic"));
        assertEquals("AT_LEAST_ONCE", map.get("qos"));
        assertEquals(Boolean.toString(true), map.get("retain"));

        assertNull(map.get("contentType"));
        assertNull(map.get("payloadFormatIndicator"));
        assertNull(map.get("messageExpiryInterval"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("correlationData"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("userProperties"));
    }

    @Test
    void mqtt5_base64Payload_allFieldPresent_converted() {
        final Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(BYTES);
        when(publish.getTopic()).thenReturn(MqttTopic.of("myTopic"));
        when(publish.getQos()).thenReturn(MqttQos.AT_LEAST_ONCE);
        when(publish.isRetain()).thenReturn(true);

        when(publish.getContentType()).thenReturn(Optional.of(MqttUtf8String.of("Base64")));
        when(publish.getPayloadFormatIndicator()).thenReturn(Optional.of(UNSPECIFIED));
        when(publish.getMessageExpiryInterval()).thenReturn(OptionalLong.of(1337));
        when(publish.getResponseTopic()).thenReturn(Optional.of(MqttTopic.of("myResponseTopic")));
        when(publish.getCorrelationData()).thenReturn(Optional.of(ByteBuffer.wrap(BYTES).asReadOnlyBuffer()));

        final Mqtt5UserProperties userProperties = Mqtt5UserProperties.of(
                Mqtt5UserProperty.of("name1", "value1"),
                Mqtt5UserProperty.of("name2", "value2"));
        when(publish.getUserProperties()).thenReturn(userProperties);

        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(publish, false);
        System.out.println(jsonMqttPublish);
        final Map<String, Object> map = GSON.fromJson(jsonMqttPublish.toString(), new TypeToken<Map<String, Object>>() {}.getType());

        assertEquals("hello", map.get("payload"));
        assertEquals("myTopic", map.get("topic"));
        assertEquals("AT_LEAST_ONCE", map.get("qos"));
        assertEquals(true, map.get("retain"));

        assertEquals("Base64", map.get("contentType"));
        assertEquals("UNSPECIFIED", map.get("payloadFormatIndicator"));
        assertEquals(1337.0, map.get("messageExpiryInterval"));
        assertEquals("myResponseTopic", map.get("responseTopic"));
        assertEquals("hello", map.get("correlationData"));
        assertEquals("myResponseTopic", map.get("responseTopic"));

        final Object userPropertiesMap = map.get("userProperties");
        assertTrue(userPropertiesMap instanceof Map);
        @SuppressWarnings("unchecked")
        final Map<String, String> userPropertiesMapCasted = (Map<String, String>) userPropertiesMap;
        assertEquals("value1", userPropertiesMapCasted.get("name1"));
        assertEquals("value2", userPropertiesMapCasted.get("name2"));
    }

    @Test
    void mqtt5_utf8Payload_noFieldsPresent_converted() {
        final Mqtt5Publish publish = mock(Mqtt5Publish.class);
        when(publish.getPayloadAsBytes()).thenReturn(BYTES);
        when(publish.getTopic()).thenReturn(MqttTopic.of("myTopic"));
        when(publish.getQos()).thenReturn(MqttQos.AT_LEAST_ONCE);
        when(publish.isRetain()).thenReturn(true);

        when(publish.getContentType()).thenReturn(Optional.empty());
        when(publish.getPayloadFormatIndicator()).thenReturn(Optional.empty());
        when(publish.getMessageExpiryInterval()).thenReturn(OptionalLong.empty());
        when(publish.getResponseTopic()).thenReturn(Optional.empty());
        when(publish.getCorrelationData()).thenReturn(Optional.empty());
        when(publish.getUserProperties()).thenReturn(Mqtt5UserProperties.of());

        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(publish, false);
        final Map<String, String> map = GSON.fromJson(jsonMqttPublish.toString(), STRING_STRING_MAP);

        assertEquals("hello", map.get("payload"));
        assertEquals("myTopic", map.get("topic"));
        assertEquals("AT_LEAST_ONCE", map.get("qos"));
        assertEquals(Boolean.toString(true), map.get("retain"));

        assertNull(map.get("contentType"));
        assertNull(map.get("payloadFormatIndicator"));
        assertNull(map.get("messageExpiryInterval"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("correlationData"));
        assertNull(map.get("responseTopic"));
        assertNull(map.get("userProperties"));
    }
}