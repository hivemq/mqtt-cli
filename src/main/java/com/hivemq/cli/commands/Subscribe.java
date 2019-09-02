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
package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface Subscribe extends Context {

    String[] getTopics();

    void setTopics(final String[] topics);

    MqttQos[] getQos();

    void setQos(final MqttQos[] qos);

    File getReceivedMessagesFile();

    void setReceivedMessagesFile(@Nullable final File receivedMessagesFile);

    boolean isPrintToSTDOUT();

    void setPrintToSTDOUT(final boolean printToSTDOUT);

    boolean isBase64();

    void setBase64(final boolean base64);

    @Nullable Mqtt5UserProperties getSubscribeUserProperties();

    void setSubscribeUserProperties(@Nullable final Mqtt5UserProperties subscribeUserProperties);
}
