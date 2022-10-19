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

package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

public abstract class AbstractConnectRestrictionFlags extends AbstractWillFlags implements ConnectRestrictions {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--rcvMax"}, description =
            "The maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM + ")", order = 3)
    private @Nullable Integer receiveMaximum;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--sendMax"}, description =
            "The maximum amount of not acknowledged publishes with QoS 1 or 2 the client send to the server concurrently. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_SEND_MAXIMUM + ")", order = 3)
    private @Nullable Integer sendMaximum;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--maxPacketSize"},
            description = "The maximum packet size the client accepts from the server. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE + ")", order = 3)
    private @Nullable Integer maximumPacketSize;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--sendMaxPacketSize"},
            description = "The maximum packet size the client sends to the server. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_SEND_MAXIMUM_PACKET_SIZE + ")", order = 3)
    private @Nullable Integer sendMaximumPacketSize;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--topicAliasMax"},
            description = "The maximum amount of topic aliases the client accepts from the server. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM + ")", order = 3)
    private @Nullable Integer topicAliasMaximum;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--sendTopicAliasMax"},
            description = "The maximum amount of topic aliases the client sends to the server. (default: " +
                    Mqtt5ConnectRestrictions.DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM + ")", order = 3)
    private @Nullable Integer sendTopicAliasMaximum;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @CommandLine.Option(names = {"--no-reqProblemInfo"}, negatable = true,
            description = "The client requests problem information from the server. (default: true)", defaultValue = "true", order = 3)
    private boolean requestProblemInformation;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @CommandLine.Option(names = {"--reqResponseInfo"},
            description = "The client requests response information from the server. (default: false)",
            defaultValue = "false", order = 3)
    private boolean requestResponseInformation;

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();

        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (receiveMaximum != null) {
                Logger.warn("Restriction receive maximum was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (sendMaximum != null) {
                Logger.warn("Restriction send maximum was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (maximumPacketSize != null) {
                Logger.warn("Restriction maximum packet size was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (sendMaximumPacketSize != null) {
                Logger.warn("Restriction send maximum packet size was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (topicAliasMaximum != null) {
                Logger.warn("Restriction topic alias maximum was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (sendTopicAliasMaximum != null) {
                Logger.warn("Restriction send topic alias maximum was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (requestProblemInformation) {
                Logger.warn("Restriction request problem information was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
            if (requestResponseInformation) {
                Logger.warn("Restriction request response information was set but is unused in MQTT Version {}",
                        MqttVersion.MQTT_3_1_1);
            }
        }
    }

    public @NotNull String connectRestrictionOptions() {
        return (receiveMaximum != null ? (", receiveMaximum=" + receiveMaximum) : "") +
                (sendMaximum != null ? (", sendMaximum=" + sendMaximum) : "") +
                (maximumPacketSize != null ? (", maximumPacketSize=" + maximumPacketSize) : "") +
                (sendMaximumPacketSize != null ? (", sendMaximumPacketSize=" + sendMaximumPacketSize) : "") +
                (topicAliasMaximum != null ? (", topicAliasMaximum=" + topicAliasMaximum) : "") +
                (sendTopicAliasMaximum != null ? (", sendTopicAliasMaximum=" + sendTopicAliasMaximum) : "") +
                ", requestProblemInformation=" + requestProblemInformation + ", requestResponseInformation=" +
                requestResponseInformation;
    }

    @Override
    public @Nullable Integer getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public @Nullable Integer getSendMaximum() {
        return sendMaximum;
    }

    @Override
    public @Nullable Integer getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public @Nullable Integer getSendMaximumPacketSize() {
        return sendMaximumPacketSize;
    }

    @Override
    public @Nullable Integer getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public @Nullable Integer getSendTopicAliasMaximum() {
        return sendTopicAliasMaximum;
    }

    @Override
    public @Nullable Boolean getRequestProblemInformation() {
        return requestProblemInformation;
    }

    @Override
    public @Nullable Boolean getRequestResponseInformation() {
        return requestResponseInformation;
    }

}
