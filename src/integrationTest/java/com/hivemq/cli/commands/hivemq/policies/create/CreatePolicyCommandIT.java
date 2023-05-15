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

package com.hivemq.cli.commands.hivemq.policies.create;

import com.hivemq.cli.utils.TestLoggerUtils;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;

class CreatePolicyCommandIT {

    public static final int HTTP_PORT = 8888;

    @RegisterExtension
    final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension(DockerImageName.parse("hivemq/hivemq4")).withHiveMQConfig(MountableFile.forClasspathResource(
                            "hivemq.configs/rest-api-config.xml"))
                    .withExposedPorts(HiveMQTestContainerExtension.MQTT_PORT, HTTP_PORT);

    private @NotNull File file;

    @BeforeEach
    void setUp() throws IOException {
        TestLoggerUtils.resetLogger();
        file = File.createTempFile("client_details", ".csv");
    }

    @AfterEach
    void tearDown() {
        file.deleteOnExit();
    }

    // TODO

}
