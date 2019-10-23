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
package com.hivemq.cli.mqtt;

import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ClientData {

    @NotNull private MqttClient mqttClient;
    private LocalDateTime creationTime;
    private Set<MqttTopicFilter> subscribedTopics;


    public ClientData(final @NotNull MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        this.creationTime = LocalDateTime.now();
        this.subscribedTopics = new HashSet<>();
    }

    public ClientData(final @NotNull MqttClient mqttClient, final @NotNull LocalDateTime creationTime) {
        this.mqttClient = mqttClient;
        this.creationTime = creationTime;
        this.subscribedTopics = new HashSet<>();
    }

    public ClientData(final @NotNull MqttClient mqttClient, final @NotNull LocalDateTime creationTime, final @NotNull Set<MqttTopicFilter> subscribedTopics) {
        this.mqttClient = mqttClient;
        this.creationTime = creationTime;
        this.subscribedTopics = subscribedTopics;
    }

    public void addSubscription(final @NotNull MqttTopicFilter topic) {
        subscribedTopics.add(topic);
    }

    public boolean removeSubscription(final @NotNull MqttTopicFilter topic) {
        return subscribedTopics.remove(topic);
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Set<MqttTopicFilter> getSubscribedTopics() {
        return subscribedTopics;
    }

    public void setSubscribedTopics(final Set<MqttTopicFilter> subscribedTopics) {
        this.subscribedTopics = subscribedTopics;
    }

    public @NotNull MqttClient getClient() {
        return this.mqttClient;
    }

    public void setClient(final @NotNull MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

}
