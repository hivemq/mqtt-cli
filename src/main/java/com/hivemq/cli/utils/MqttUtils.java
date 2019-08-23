package com.hivemq.cli.utils;

import com.hivemq.client.mqtt.datatypes.MqttQos;

import java.util.Arrays;

public class MqttUtils {


    // Arrange the size of qos array to match the topics array size
    // The returned qos array will be filled with the default value represented by the first element in the given qos array
    // if the sizes dont match up. Else this method throws an IllegalArgument exception
    public static MqttQos[] arrangeQosToMatchTopics(final String[] topics, MqttQos[] qos) {
        if (topics.length != qos.length && qos.length == 1) {
            final MqttQos defaultQos = qos[0];
            qos = new MqttQos[topics.length];
            Arrays.fill(qos, defaultQos);
            return qos;
        } else if (topics.length == qos.length) {
            return qos;
        }
        throw new IllegalArgumentException("Topics do not match up to the QoS given. Topics Size {" + topics.length + "}, QoS Size {" + qos.length + "}");
    }
}
