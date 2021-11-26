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

import com.hivemq.cli.mqtt.test.results.QosTestResult;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Mqtt3FeatureTesterQos1Test {

    static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4").withTag("4.4.0"))
                    .withHiveMQConfig(MountableFile.forClasspathResource("mqtt/test/qos1-config.xml"));

    static Mqtt3FeatureTester mqtt3FeatureTester;

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
        mqtt3FeatureTester = new Mqtt3FeatureTester(hivemq.getContainerIpAddress(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
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
    void qos_2_failed() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.EXACTLY_ONCE, 10);
        assertEquals(0, qosTestResult.getReceivedPublishes());
    }
}