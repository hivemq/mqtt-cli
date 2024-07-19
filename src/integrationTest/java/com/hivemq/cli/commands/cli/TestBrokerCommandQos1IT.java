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

package com.hivemq.cli.commands.cli;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.utils.TestLoggerUtils;
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Tests are only used to check output")
@Testcontainers
class TestBrokerCommandQos1IT {

    @Container
    private final @NotNull HiveMQContainer hivemq =
            new HiveMQContainer(OciImages.getImageName("hivemq/hivemq4")).withHiveMQConfig(MountableFile.forClasspathResource(
                    "mqtt/test/qos1-config.xml"));

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
    }

    @Test
    void qos1_restricted_mqtt3_features() {
        assertEquals(0, MqttCLIMain.mainWithExitCode("test", "-V", "3", "-p", String.valueOf(hivemq.getMqttPort())));
    }

    @Test
    void qos1_restricted_mqtt5_features() {
        assertEquals(0,
                MqttCLIMain.mainWithExitCode("test", "-V", "5", "-a", "-p", String.valueOf(hivemq.getMqttPort())));
    }
}
