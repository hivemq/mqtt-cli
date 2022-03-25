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

package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

public interface Publish extends Context {

    @NotNull String @NotNull [] getTopics();

    @NotNull MqttQos @NotNull [] getQos();

    @NotNull ByteBuffer getMessage();

    @Nullable Boolean getRetain();

    @Nullable Long getMessageExpiryInterval();

    @Nullable Mqtt5PayloadFormatIndicator getPayloadFormatIndicator();

    @Nullable String getContentType();

    @Nullable String getResponseTopic();

    @Nullable ByteBuffer getCorrelationData();

    @Nullable Mqtt5UserProperties getUserProperties();

}
