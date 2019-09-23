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
package com.hivemq.cli.commands.cli;

import com.hivemq.cli.commands.AbstractCommonFlags;
import com.hivemq.cli.commands.Connect;
import com.hivemq.cli.converters.Mqtt5UserPropertyConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.utils.MqttUtils;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.Nullable;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

public class AbstractConnectFlags extends AbstractCommonFlags implements Connect {

    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, converter = UnsignedIntConverter.class, description = "The lifetime of the session of the connected client", order = 2)
    @Nullable
    private Long sessionExpiryInterval;

    @CommandLine.Option(names = {"-Cup", "--connectUserProperty"}, converter = Mqtt5UserPropertyConverter.class, description = "A user property of the connect message'", order = 2)
    @Nullable
    private Mqtt5UserProperty[] connectUserProperties;


    String connectOptions() {
        return commonOptions() +
                ", sessionExpiryInterval= " + sessionExpiryInterval +
                ", userProperties=" + connectUserProperties +
                ", " + connectRestrictionOptions();

    }

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();
        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (sessionExpiryInterval != null) {
                Logger.warn("Connect session expiry interval was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (connectUserProperties != null) {
                Logger.warn("Connect user properties were set but are unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    @Nullable
    public Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(@Nullable final Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Nullable
    public Mqtt5UserProperties getConnectUserProperties() {
        return MqttUtils.convertToMqtt5UserProperties(connectUserProperties);
    }

    public void setConnectUserProperties(@Nullable final Mqtt5UserProperty... connectUserProperties) {
        this.connectUserProperties = connectUserProperties;
    }

}
