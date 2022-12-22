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

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Disabled("Tests are only used to check output")
class TestBrokerCommandQos0IT {

    private static final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4")).withHiveMQConfig(MountableFile.forClasspathResource(
                    "mqtt/test/qos0-config.xml"));

    @BeforeAll
    static void beforeAll() {
        hivemq.start();
    }

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
    }

    @AfterAll
    static void afterAll() {
        hivemq.stop();
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void qos0_restricted_mqtt3_features() {
        MqttCLIMain.main("test", "-V", "3", "-p", String.valueOf(hivemq.getMqttPort()));
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void qos0_restricted_mqtt5_features() {
        MqttCLIMain.main("test", "-V", "3", "-p", String.valueOf(hivemq.getMqttPort()));
    }
}
