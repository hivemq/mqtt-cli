/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
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

@CommandLine.Command(name = "unsub",
        aliases = "unsubscribe",
        description = "Unsubscribes this mqtt client from a list of topics")

public class ContextUnsubscribeCommand extends ShellContextCommand implements Runnable, Unsubscribe {

    //needed for pico cli - reflection code generation
    public ContextUnsubscribeCommand(){
        this(null);
    }

    @Inject
    public ContextUnsubscribeCommand(@NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    @NotNull private String[] topics;

    @CommandLine.Option(names = {"-up", "--userProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property for the unsubscribe message")
    @Nullable
    private Mqtt5UserProperty[] userProperties;

    @Override
    public void run() {

        Logger.trace("Command {} ", this);

        logUnusedUnsubscribeOptions();

        try {
            mqttClientExecutor.unsubscribe(contextClient, this);
        }
        catch (final Exception ex) {
            Logger.error(ex, Throwables.getRootCause(ex).getMessage());
        }
    }

    private void logUnusedUnsubscribeOptions() {
        if (contextClient.getConfig().getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            if (userProperties != null) {
                Logger.warn("Unsubscribe user properties were set but are unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                (userProperties != null ? (", userProperties=" + Arrays.toString(userProperties)) : "") +
                '}';
    }

    @Override
    @NotNull
    public String[] getTopics() {
        return topics;
    }

    @Override
    @Nullable
    public Mqtt5UserProperties getUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(userProperties);
    }

    public void setUserProperties(final Mqtt5UserProperty... userProperties) {
        this.userProperties = userProperties;
    }
}
