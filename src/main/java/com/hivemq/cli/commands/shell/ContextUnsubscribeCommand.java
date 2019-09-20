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

import com.hivemq.cli.commands.Unsubscribe;
import com.hivemq.cli.converters.UserPropertiesConverter;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
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

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "The topics to publish to")
    @NotNull
    private String[] topics;

    @CommandLine.Option(names = {"-up", "--userProperties"}, converter = UserPropertiesConverter.class, description = "The user Properties of the unsubscribe message (Usage: 'Key=Value', 'Key1=Value1|Key2=Value2')")
    @Nullable
    private Mqtt5UserProperties userProperties;


    @Override
    public void run() {
        if (isVerbose()) {
            Logger.trace("Command {} ", this);
        }

        try {
            mqttClientExecutor.unsubscribe(contextClient, this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());
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
        return "ContextUnsubscribe:: {" +
                "key=" + getKey() +
                ", topics=" + Arrays.toString(topics) +
                ", userProperties=" + userProperties +
                '}';
    }

    @Override
    @NotNull
    public String[] getTopics() {
        return topics;
    }

    public void setTopics(final String[] topics) {
        this.topics = topics;
    }

    @Override
    @Nullable
    public Mqtt5UserProperties getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(final Mqtt5UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
