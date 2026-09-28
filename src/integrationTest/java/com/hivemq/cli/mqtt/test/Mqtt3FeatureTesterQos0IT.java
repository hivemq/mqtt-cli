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
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class Mqtt3FeatureTesterQos0IT {

    // Debug variants: "baseline", "malloc-arena-2" (MALLOC_ARENA_MAX=2), "jdk25" (the image with the JRE of
    // eclipse-temurin:25-jre first on the PATH), "cross" (4.55.0 broker and JDK on the 4.54.0 base image).
    private static final @NotNull String VARIANT = "cross";

    @Container
    private final @NotNull HiveMQContainer hivemq = container();

    private @NotNull Mqtt3FeatureTester mqtt3FeatureTester;

    private static @NotNull HiveMQContainer container() {
        final HiveMQContainer container = new HiveMQContainer(image()) //
                .withHiveMQConfig(MountableFile.forClasspathResource("mqtt/test/qos0-config.xml"))
                .withEnv("HIVEMQ_LOG_LEVEL", "TRACE")
                .withLogConsumer(outputFrame -> System.out.print("HIVEMQ: " + outputFrame.getUtf8String()));
        if (VARIANT.equals("malloc-arena-2")) {
            container.withEnv("MALLOC_ARENA_MAX", "2");
        }
        return container;
    }

    private static @NotNull DockerImageName image() {
        final DockerImageName base = OciImages.getImageName("hivemq/hivemq4");
        if (VARIANT.equals("cross")) {
            final String name = new ImageFromDockerfile("hivemq-455-on-2404-debug", false).withFileFromString(
                    "Dockerfile",
                    "FROM hivemq/hivemq4:4.54.0\n" +
                            "USER root\n" +
                            "RUN rm -rf /opt/java/openjdk && find /opt/hivemq -mindepth 1 -maxdepth 1 ! -name data ! -name log -exec rm -rf {} +\n" +
                            "COPY --from=hivemq/hivemq4:4.55.0 /opt/java/openjdk /opt/java/openjdk\n" +
                            "COPY --from=hivemq/hivemq4:4.55.0 /opt/hivemq /opt/hivemq\n" +
                            "COPY --from=hivemq/hivemq4:4.55.0 /opt/docker-entrypoint.sh /opt/docker-entrypoint.sh\n" +
                            "RUN chmod 775 /opt/hivemq\n" +
                            "USER 10000\n").get();
            return DockerImageName.parse(name).asCompatibleSubstituteFor("hivemq/hivemq4");
        }
        if (!VARIANT.equals("jdk25")) {
            return base;
        }
        final String name = new ImageFromDockerfile("hivemq-jdk25-debug", false).withFileFromString("Dockerfile",
                "FROM eclipse-temurin:25-jre AS jdk\n" +
                        "FROM " + base.asCanonicalNameString() + "\n" +
                        "COPY --from=jdk /opt/java/openjdk /opt/java/openjdk-25\n" +
                        "ENV JAVA_HOME=/opt/java/openjdk-25 PATH=/opt/java/openjdk-25/bin:$PATH\n").get();
        return DockerImageName.parse(name).asCompatibleSubstituteFor("hivemq/hivemq4");
    }

    @BeforeEach
    void setUp() {
        mqtt3FeatureTester = new Mqtt3FeatureTester(hivemq.getHost(), hivemq.getMqttPort(), null, null, null, 3);
    }

    @Test
    void qos_0_success() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.AT_MOST_ONCE, 10);
        assertEquals(10, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_1_failed() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.AT_LEAST_ONCE, 10);
        assertEquals(0, qosTestResult.getReceivedPublishes());
    }

    @Test
    void qos_2_failed() {
        final QosTestResult qosTestResult = mqtt3FeatureTester.testQos(MqttQos.EXACTLY_ONCE, 10);
        assertEquals(0, qosTestResult.getReceivedPublishes());
    }
}
