package com.hivemq.cli.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ClientData {

    private LocalDateTime creationTime;
    private Set<MqttTopic> subscribedTopics;

    public ClientData(final @NotNull LocalDateTime creationTime) {
        this.creationTime = creationTime;
        subscribedTopics = new HashSet<>();
    }

    public void addSubscription(final @NotNull MqttTopic topic) {
        subscribedTopics.add(topic);
    }

    public boolean removeSubscription(final @NotNull MqttTopic topic) {
        return subscribedTopics.remove(topic);
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(final LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Set<MqttTopic> getSubscribedTopics() {
        return subscribedTopics;
    }

    public void setSubscribedTopics(final Set<MqttTopic> subscribedTopics) {
        this.subscribedTopics = subscribedTopics;
    }
}
