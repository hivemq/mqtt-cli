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

package com.hivemq.cli.commands.shell;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

@CommandLine.Command(name = "unsub", aliases = "unsubscribe",
        description = "Unsubscribe this MQTT client from a list of topics")
public class ContextUnsubscribeCommand extends ShellContextCommand implements Runnable, Unsubscribe {

    @SuppressWarnings("unused") //needed for pico cli - reflection code generation
    public ContextUnsubscribeCommand() {
        //noinspection ConstantConditions
        this(null);
    }

    @Inject
    public ContextUnsubscribeCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"}) //will be initialized via required
    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    private @NotNull String @NotNull [] topics;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class,
            description = "A user property for the unsubscribe message")
    private @Nullable Mqtt5UserProperty @Nullable [] userProperties;

    @Override
    public void run() {
        Logger.trace("Command {} ", this);

        logUnusedUnsubscribeOptions();

        try {
            mqttClientExecutor.unsubscribe(Objects.requireNonNull(contextClient), this);
        } catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
    }

    private void logUnusedUnsubscribeOptions() {
        if (Objects.requireNonNull(contextClient).getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn(
                        "Unsubscribe user properties were set but are unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + "{" + "key=" + getKey() + ", topics=" + Arrays.toString(topics) +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") + '}';
    }

    @Override
    public @NotNull String @NotNull [] getTopics() {
        return topics;
    }

    @Override
    public @Nullable Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }
}
