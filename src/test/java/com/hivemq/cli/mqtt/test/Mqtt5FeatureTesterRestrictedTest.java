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
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static com.hivemq.cli.mqtt.test.results.TestResult.PUBLISH_FAILED;
import static com.hivemq.cli.mqtt.test.results.TestResult.SUBSCRIBE_FAILED;
import static org.junit.jupiter.api.Assertions.*;

class Mqtt5FeatureTesterRestrictedTest {

    static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4").withTag("4.4.0"))
                    .withHiveMQConfig(MountableFile.forClasspathResource("mqtt/test/restricted-config.xml"));

    static Mqtt5FeatureTester mqtt5FeatureTester;

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
        mqtt5FeatureTester = new Mqtt5FeatureTester(hivemq.getContainerIpAddress(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    void wildcard_subscriptions_failed() {
        final WildcardSubscriptionsTestResult wildcardSubscriptionsTestResult = mqtt5FeatureTester.testWildcardSubscriptions();
        assertEquals(SUBSCRIBE_FAILED, wildcardSubscriptionsTestResult.getHashWildcardTest());
        assertEquals(SUBSCRIBE_FAILED, wildcardSubscriptionsTestResult.getPlusWildcardTest());
        assertFalse(wildcardSubscriptionsTestResult.isSuccess());
    }

    @Test
    void shared_subscriptions_failed() {
        final SharedSubscriptionTestResult sharedSubscriptionTestResult = mqtt5FeatureTester.testSharedSubscription();
        assertEquals(SharedSubscriptionTestResult.SUBSCRIBE_FAILED, sharedSubscriptionTestResult);
    }

    @Test
    void retain_failed() {
        mqtt5FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final TestResult testResult = mqtt5FeatureTester.testRetain();
        assertEquals(PUBLISH_FAILED, testResult);
    }

    @Test
    void payload_size_1MB_failed_max_500KB() {
        mqtt5FeatureTester.setMaxQos(MqttQos.AT_LEAST_ONCE);
        final PayloadTestResults payloadTestResults = mqtt5FeatureTester.testPayloadSize(100_000);
        assertTrue(payloadTestResults.getPayloadSize() < 100_000);
    }

    @Test
    @Disabled("HiveMQ currently ignores topic length restriction in its config")
    void topic_length_failed_max_30() {
        final TopicLengthTestResults topicLengthTestResults = mqtt5FeatureTester.testTopicLength();
        assertEquals(30, topicLengthTestResults.getMaxTopicLength());
    }

    @Test
    void clientId_length_failed_max_30() {
        final ClientIdLengthTestResults clientIdLengthTestResults = mqtt5FeatureTester.testClientIdLength();
        assertEquals(30, clientIdLengthTestResults.getMaxClientIdLength());
    }
}