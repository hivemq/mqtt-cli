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
package com.hivemq.cli.mqtt.clients.listeners;

import com.google.common.base.Throwables;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class LogDisconnectedListener implements MqttClientDisconnectedListener {

    public static final @NotNull LogDisconnectedListener INSTANCE = new LogDisconnectedListener();

    private LogDisconnectedListener() {
    }

    @Override
    public void onDisconnected(@NotNull final MqttClientDisconnectedContext context) {
        if (context.getSource() != MqttDisconnectSource.USER) {
            final Throwable cause = context.getCause();

            Logger.debug(cause,
                    "{} DISCONNECTED {}",
                    LoggerUtils.getClientPrefix(context.getClientConfig()),
                    Throwables.getRootCause(cause).getMessage());
        }
    }
}
