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

import com.hivemq.cli.mqtt.test.results.AsciiCharsInClientIdTestResults;
import com.hivemq.cli.mqtt.test.results.ClientIdLengthTestResults;
import com.hivemq.cli.mqtt.test.results.PayloadTestResults;
import com.hivemq.cli.mqtt.test.results.QosTestResult;
import com.hivemq.cli.mqtt.test.results.SharedSubscriptionTestResult;
import com.hivemq.cli.mqtt.test.results.TestResult;
import com.hivemq.cli.mqtt.test.results.TopicLengthTestResults;
import com.hivemq.cli.mqtt.test.results.WildcardSubscriptionsTestResult;
import com.hivemq.cli.utils.Tuple;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.hivemq.cli.mqtt.test.results.TestResult.OK;
import static com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class Mqtt3FeatureTesterDefaultIT {

    @Container
    private final @NotNull HiveMQContainer hivemq = new HiveMQContainer(OciImages.getImageName("hivemq/hivemq4"));

    private @NotNull Mqtt3FeatureTester mqtt3FeatureTester;

    @BeforeEach
    void setUp() {
        mqtt3FeatureTester = new Mqtt3FeatureTester(hivemq.getHost(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @Test
    void connect_success() {
        final Mqtt3ConnAck connAck = mqtt3FeatureTester.testConnect();
        assertNotNull(connAck);
        assertEquals(SUCCESS, connAck.getReturnCode());
    }

    @Test
    void connect_failed() {
        final Mqtt3FeatureTester featureTester = new Mqtt3FeatureTester("localhost", 1883, null, null, null, 30);
        featureTester.setMaxTopicLength(30);
        assertThrows(ConnectionFailedException.class, featureTester::testConnect);
    }

    @Test
    void wildcard_subscriptions_success() {
        final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult =
                mqtt3FeatureTester.testWildcardSubscriptions();
        assertEquals(OK, wildcardSubscriptionsTestResult.getHashWildcardTest());
        assertEquals(OK, wildcardSubscriptionsTestResult.getPlusWildcardTest());
        assertTrue(wildcardSubscriptionsTestResult.isSuccess());
    }

    @Test
    void shared_subscriptions_success() {
        final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt3FeatureTester.testSharedSubscription();
        assertEquals(SharedSubscriptionTestResult.OK, sharedSubscriptionTestResult);
    }

    @Test
    void retain_success() {
        final TestResult testResult = mqtt3FeatureTester.testRetain();
        assertEquals(OK, testResult);
    }

    @Test
    void qos_0_success() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.AT_MOST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_1_success() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.AT_LEAST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_2_success() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.EXACTLY_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void payload_size_1MB_success() {
        final PayloadTestResults payloadTestResults = mqtt3FeatureTester.testPayloadSize(100_000);
        assertEquals(100_000, payloadTestResults.getPayloadSize());
        for (final Tuple<Integer, TestResult> testResult : payloadTestResults.getTestResults()) {
            assertEquals(OK, testResult.getValue());
        }
    }

    @Test
    void topic_length_success() {
        final TopicLengthTestResults topicLengthTestResults = mqtt3FeatureTester.testTopicLength();
        assertEquals(65535, topicLengthTestResults.getMaxTopicLength());
        for (final Tuple<Integer, TestResult> testResult : topicLengthTestResults.getTestResults()) {
            assertEquals(OK, testResult.getValue());
        }
    }

    @Test
    void clientId_length_success() {
        final ClientIdLengthTestResults clientIdLengthTestResults = mqtt3FeatureTester.testClientIdLength();
        assertEquals(65535, clientIdLengthTestResults.getMaxClientIdLength());
        for (final Tuple<Integer, String> testResult : clientIdLengthTestResults.getTestResults()) {
            assertEquals(Mqtt3ConnAckReturnCode.SUCCESS.toString(), testResult.getValue());
        }
    }

    @Test
    void asciiChars_success() {
        final AsciiCharsInClientIdTestResults asciiCharsInClientIdTestResults =
                mqtt3FeatureTester.testAsciiCharsInClientId();
        for (final Tuple<Character, String> testResult : asciiCharsInClientIdTestResults.getTestResults()) {
            assertEquals(Mqtt3ConnAckReturnCode.SUCCESS.toString(), testResult.getValue());
        }
        assertTrue(asciiCharsInClientIdTestResults.getUnsupportedChars().isEmpty());
    }
}
