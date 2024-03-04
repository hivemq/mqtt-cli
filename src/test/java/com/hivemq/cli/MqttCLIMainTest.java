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
package com.hivemq.cli;

import com.hivemq.cli.utils.TestLoggerUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MqttCLIMainTest {

    @BeforeEach
    void setUp() {
        TestLoggerUtils.resetLogger();
    }

    @AfterEach
    void tearDown() {
        TestLoggerUtils.resetLogger();
    }

    @Test
    void mqtt_command() {
        assertEquals(0, MqttCLIMain.mainWithExitCode());
    }

    @Test
    void hivemq_command() {
        assertEquals(0, MqttCLIMain.mainWithExitCode("hivemq"));
    }

    @Test
    void hivemq_export_command() {
        assertEquals(0, MqttCLIMain.mainWithExitCode("hivemq", "export"));
    }

    @Test
    void hivemq_export_clients_help_command() {
        assertEquals(0, MqttCLIMain.mainWithExitCode("hivemq", "export", "clients", "-h"));
    }
}
