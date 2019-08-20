package com.hivemq.cli.utils;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MqttUtilsTest {

    @Test
    void testArrangeQosToMatchTopics_Success_One_QoS() {

        String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE};

        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);

        assertEquals(topics.length, qos.length);

        MqttQos[] expected = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE};

        assertArrayEquals(qos, expected);
    }

    @Test
    void testArrangeQosToMatchTopics_Success_Full_QoS() {

        String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.EXACTLY_ONCE};

        qos = MqttUtils.arrangeQosToMatchTopics(topics, qos);

        assertEquals(topics.length, qos.length);

        MqttQos[] expected = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.EXACTLY_ONCE};

        assertArrayEquals(qos, expected);
    }

    @Test
    void testArrangeQosToMatchTopics_Failure_Wrong_QoS_Amount() {

        String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE};

        assertThrows(IllegalArgumentException.class, () -> MqttUtils.arrangeQosToMatchTopics(topics, qos));

    }

    @Test
    void testArrangeQosToMatchTopics_Failure_Too_Many_QoS() {

        String[] topics = {"topic/subtopic1", "topic/subtopic2", "topic/subtopic3"};
        MqttQos[] qos = {MqttQos.AT_LEAST_ONCE, MqttQos.AT_MOST_ONCE, MqttQos.AT_LEAST_ONCE, MqttQos.AT_LEAST_ONCE};

        assertThrows(IllegalArgumentException.class, () -> MqttUtils.arrangeQosToMatchTopics(topics, qos));

    }

}