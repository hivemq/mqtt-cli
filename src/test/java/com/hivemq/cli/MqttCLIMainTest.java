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

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.hivemq.cli.utils.TestLoggerUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @ExpectSystemExitWithStatus(0)
    void mqtt_command() {
        MqttCLIMain.main();
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_command() {
        MqttCLIMain.main("hivemq");
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_export_command() {
        MqttCLIMain.main("hivemq", "export");
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_export_clients_help_command() {
        MqttCLIMain.main("hivemq", "export", "clients", "-h");
    }
}
