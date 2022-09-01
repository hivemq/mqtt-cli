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

package com.hivemq.cli.mqtt.test;

import com.hivemq.cli.mqtt.test.results.*;
import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import static com.hivemq.cli.mqtt.test.results.TestResult.OK;
import static org.junit.jupiter.api.Assertions.*;

class Mqtt5FeatureTesterDefaultIT {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4"));

    private @NotNull Mqtt5FeatureTester mqtt5FeatureTester;

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @BeforeEach
    void setUp() {
        mqtt5FeatureTester =
                new Mqtt5FeatureTester(hivemq.getHost(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    void connect_success() {
        final Mqtt5ConnAck connAck = mqtt5FeatureTester.testConnect();
        assertNotNull(connAck);
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, connAck.getReasonCode());
    }

    @Test
    void connect_failed() {
        final Mqtt5FeatureTester featureTester = new Mqtt5FeatureTester("localhost", 1883, null, null, null, 30);
        featureTester.setMaxTopicLength(30);
        assertThrows(ConnectionFailedException.class, featureTester::testConnect);
    }

    @Test
    void wildcard_subscriptions_success() {
        final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult =
                mqtt5FeatureTester.testWildcardSubscriptions();
        assertEquals(OK, wildcardSubscriptionsTestResult.getHashWildcardTest());
        assertEquals(OK, wildcardSubscriptionsTestResult.getPlusWildcardTest());
        assertTrue(wildcardSubscriptionsTestResult.isSuccess());
    }

    @Test
    void shared_subscriptions_success() {
        final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt5FeatureTester.testSharedSubscription();
        assertEquals(SharedSubscriptionTestResult.OK, sharedSubscriptionTestResult);
    }

    @Test
    void retain_success() {
        final TestResult testResult = mqtt5FeatureTester.testRetain();
        assertEquals(OK, testResult);
    }

    @Test
    void qos_0_success() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.AT_MOST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_1_success() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.AT_LEAST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_2_success() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.EXACTLY_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void payload_size_1MB_success() {
        final PayloadTestResults payloadTestResults = mqtt5FeatureTester.testPayloadSize(100_000);
        assertEquals(100_000, payloadTestResults.getPayloadSize());
        for (final Tuple<Integer, TestResult> testResult : payloadTestResults.getTestResults()) {
            assertEquals(OK, testResult.getValue());
        }
    }

    @Test
    void topic_length_success() {
        final TopicLengthTestResults topicLengthTestResults = mqtt5FeatureTester.testTopicLength();
        assertEquals(65535, topicLengthTestResults.getMaxTopicLength());
        for (final Tuple<Integer, TestResult> testResult : topicLengthTestResults.getTestResults()) {
            assertEquals(OK, testResult.getValue());
        }
    }

    @Test
    void clientId_length_success() {
        final ClientIdLengthTestResults clientIdLengthTestResults = mqtt5FeatureTester.testClientIdLength();
        assertEquals(65535, clientIdLengthTestResults.getMaxClientIdLength());
        for (final Tuple<Integer, String> testResult : clientIdLengthTestResults.getTestResults()) {
            assertEquals(Mqtt5ConnAckReasonCode.SUCCESS.toString(), testResult.getValue());
        }
    }

    @Test
    void asciiChars_success() {
        final AsciiCharsInClientIdTestResults asciiCharsInClientIdTestResults =
                mqtt5FeatureTester.testAsciiCharsInClientId();
        for (final Tuple<Character, String> testResult : asciiCharsInClientIdTestResults.getTestResults()) {
            assertEquals(Mqtt5ConnAckReasonCode.SUCCESS.toString(), testResult.getValue());
        }
        assertTrue(asciiCharsInClientIdTestResults.getUnsupportedChars().isEmpty());
    }
}