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
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class Mqtt5FeatureTesterQos0IT {

    @Container
    private final @NotNull HiveMQContainer hivemq = new HiveMQContainer(OciImages.getImageName("hivemq/hivemq4")) //
            .withHiveMQConfig(MountableFile.forClasspathResource("mqtt/test/qos0-config.xml"));

    private @NotNull Mqtt5FeatureTester mqtt5FeatureTester;

    @BeforeEach
    void setUp() {
        mqtt5FeatureTester = new Mqtt5FeatureTester(hivemq.getHost(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @Test
    void qos_0_success() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.AT_MOST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_1_failed() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.AT_LEAST_ONCE, 10);
        assertEquals(0, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_2_failed() {
        final QosTestResult qosTestResult = mqtt5FeatureTester.testQos(MqttQos.EXACTLY_ONCE, 10);
        assertEquals(0, qosTestResult.getReceivedPublishes());
    }
}
