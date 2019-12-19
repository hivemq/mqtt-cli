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
package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

public abstract class AbstractConnectRestrictionFlags extends AbstractWillFlags implements ConnectRestrictions {

    @CommandLine.Option(names = {"--rcvMax"}, description = "The maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently. (default: " + Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM + ")", order = 3)
    @Nullable
    private Integer receiveMaximum;

    @CommandLine.Option(names = {"--sendMax"}, description = "The maximum amount of not acknowledged publishes with QoS 1 or 2 the client send to the server concurrently. (default: " + Mqtt5ConnectRestrictions.DEFAULT_SEND_MAXIMUM + ")", order = 3)
    @Nullable
    private Integer sendMaximum;

    @CommandLine.Option(names = {"--maxPacketSize"}, description = "The maximum packet size the client accepts from the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE + ")", order = 3)
    @Nullable
    private Integer maximumPacketSize;

    @CommandLine.Option(names = {"--sendMaxPacketSize"}, description = "The maximum packet size the client sends to the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_SEND_MAXIMUM_PACKET_SIZE + ")", order = 3)
    @Nullable
    private Integer sendMaximumPacketSize;

    @CommandLine.Option(names = {"--topicAliasMax"}, description = "The maximum amount of topic aliases the client accepts from the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM + ")", order = 3)
    @Nullable
    private Integer topicAliasMaximum;

    @CommandLine.Option(names = {"--sendTopicAliasMax"}, description = "The maximum amount of topic aliases the client sends to the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_SEND_TOPIC_ALIAS_MAXIMUM + ")", order = 3)
    @Nullable
    private Integer sendTopicAliasMaximum;

    @CommandLine.Option(names = {"--reqProblemInfo"}, negatable = true, description = "The client requests problem information from the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION + ")", order = 3)
    @Nullable
    private Boolean requestProblemInformation;

    @CommandLine.Option(names = {"--reqResponseInfo"}, negatable = true, description = "The client requests response information from the server. (default: " + Mqtt5ConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION + ")", order = 3)
    @Nullable
    private Boolean requestResponseInformation;

    @Override
    public void logUnusedOptions() {
        super.logUnusedOptions();


        if (getVersion() == MqttVersion.MQTT_3_1_1) {
            if (receiveMaximum != null) {
                Logger.warn("Restriction receive maximum was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (sendMaximum != null) {
                Logger.warn("Restriction send maximum was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (maximumPacketSize != null) {
                Logger.warn("Restriction maximum packet size was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (sendMaximumPacketSize != null) {
                Logger.warn("Restriction send maximum packet size was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (topicAliasMaximum != null) {
                Logger.warn("Restriction topic alias maximum was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (sendTopicAliasMaximum != null) {
                Logger.warn("Restriction send topic alias maximum was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (requestProblemInformation != null) {
                Logger.warn("Restriction request problem information was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }

            if (requestResponseInformation != null) {
                Logger.warn("Restriction request response information was set but is unused in MQTT Version {}", MqttVersion.MQTT_3_1_1);
            }
        }
    }

    public String connectRestrictionOptions() {
        return (receiveMaximum != null ? (", receiveMaximum=" + receiveMaximum) : "") +
                (sendMaximum != null ? (", sendMaximum=" + sendMaximum) : "") +
                (maximumPacketSize != null ? (", maximumPacketSize=" + maximumPacketSize) : "") +
                (sendMaximumPacketSize != null ? (", sendMaximumPacketSize=" + sendMaximumPacketSize) : "") +
                (topicAliasMaximum != null ? (", topicAliasMaximum=" + topicAliasMaximum) : "") +
                (sendTopicAliasMaximum != null ? (", sendTopicAliasMaximum=" + sendTopicAliasMaximum) : "") +
                (requestProblemInformation != null ? (", requestProblemInformation=" + requestProblemInformation) : "") +
                (requestResponseInformation != null ? (", requestResponseInformation=" + requestResponseInformation) : "");
    }

    @Nullable
    @Override
    public Integer getReceiveMaximum() {
        return receiveMaximum;
    }

    @Nullable
    @Override
    public Integer getSendMaximum() {
        return sendMaximum;
    }

    @Nullable
    @Override
    public Integer getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Nullable
    @Override
    public Integer getSendMaximumPacketSize() {
        return sendMaximumPacketSize;
    }

    @Nullable
    @Override
    public Integer getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Nullable
    @Override
    public Integer getSendTopicAliasMaximum() {
        return sendTopicAliasMaximum;
    }

    @Nullable
    @Override
    public Boolean getRequestProblemInformation() {
        return requestProblemInformation;
    }

    @Nullable
    @Override
    public Boolean getRequestResponseInformation() {
        return requestResponseInformation;
    }

}
