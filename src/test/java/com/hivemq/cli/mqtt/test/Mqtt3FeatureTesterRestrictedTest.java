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
package com.hivemq.cli.mqtt.test;

import com.hivemq.cli.mqtt.test.results.ClientIdLengthTestResults;
import com.hivemq.cli.mqtt.test.results.PayloadTestResults;
import com.hivemq.cli.mqtt.test.results.SharedSubscriptionTestResult;
import com.hivemq.cli.mqtt.test.results.TestResult;
import com.hivemq.cli.mqtt.test.results.TopicLengthTestResults;
import com.hivemq.cli.mqtt.test.results.WildcardSubscriptionsTestResult;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.hivemq.cli.mqtt.test.results.TestResult.PUBLISH_FAILED;
import static com.hivemq.cli.mqtt.test.results.TestResult.SUBSCRIBE_FAILED;
import static com.hivemq.cli.mqtt.test.results.TestResult.TIME_OUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Mqtt3FeatureTesterRestrictedTest {

    final static public @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension("hivemq/hivemq4", "latest");

    static Mqtt3FeatureTester mqtt3FeatureTester;

    @BeforeAll
    static void beforeAll() {
        hivemq.withHiveMQConfig(new File("src/test/resources/mqtt/test/restricted-config.xml"));
        hivemq.start();
        mqtt3FeatureTester = new Mqtt3FeatureTester(hivemq.getContainerIpAddress(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @BeforeEach
    void setUp() {
        if (!hivemq.isRunning()) {
            hivemq.start();
        }
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }


    @Test
    void wildcard_subscriptions_failed() {
        mqtt3FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult = mqtt3FeatureTester.testWildcardSubscriptions();
        assertEquals(SUBSCRIBE_FAILED, wildcardSubscriptionsTestResult.getHashWildcardTest());
        assertEquals(SUBSCRIBE_FAILED, wildcardSubscriptionsTestResult.getPlusWildcardTest());
        assertFalse(wildcardSubscriptionsTestResult.isSuccess());
    }

    @Test
    void shared_subscriptions_failed() {
        mqtt3FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt3FeatureTester.testSharedSubscription();
        assertEquals(SharedSubscriptionTestResult.SUBSCRIBE_FAILED, sharedSubscriptionTestResult);
    }

    @Test
    void retain_failed() {
        mqtt3FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final TestResult testResult = mqtt3FeatureTester.testRetain();
        assertEquals(PUBLISH_FAILED, testResult);
    }

    @Test
    void payload_size_1MB_failed_max_500KB() {
        mqtt3FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final PayloadTestResults payloadTestResults = mqtt3FeatureTester.testPayloadSize(100_000);
        assertTrue(payloadTestResults.getPayloadSize() < 100_000);
    }

    @Test
    @Disabled("HiveMQ currently ignores topic length restriction in its config")
    void topic_length_failed_max_30() {
        final TopicLengthTestResults topicLengthTestResults = mqtt3FeatureTester.testTopicLength();
        assertEquals(30, topicLengthTestResults.getMaxTopicLength());
    }


    @Test
    void clientId_length_failed_max_30() {
        final ClientIdLengthTestResults clientIdLengthTestResults = mqtt3FeatureTester.testClientIdLength();
        assertEquals(30, clientIdLengthTestResults.getMaxClientIdLength());
    }

}
