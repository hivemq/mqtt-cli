package com.hivemq.cli;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.Test;


class MqttCLIMainTest {

    @Test
    @ExpectSystemExitWithStatus(0)
    void mqtt_command() {
        MqttCLIMain.main(new String[]{});
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_command() {
        MqttCLIMain.main(new String[]{"hivemq"});
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_export_command() {
        MqttCLIMain.main(new String[]{"hivemq", "export"});
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    void hivemq_export_clients_help_command() {
        MqttCLIMain.main(new String[]{"hivemq", "export", "clients", "-h"});
    }
}