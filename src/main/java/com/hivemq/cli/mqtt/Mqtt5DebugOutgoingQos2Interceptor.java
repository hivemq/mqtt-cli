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

package com.hivemq.cli.mqtt;

import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.internal.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class Mqtt5DebugOutgoingQos2Interceptor implements Mqtt5OutgoingQos2Interceptor {

    @Override
    public void onPubRec(
            final @NotNull Mqtt5ClientConfig clientConfig,
            final @NotNull Mqtt5Publish publish,
            final @NotNull Mqtt5PubRec pubRec,
            final @NotNull Mqtt5PubRelBuilder pubRelBuilder) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} received PUBREC\n    {}", clientPrefix, pubRec);
        Logger.debug("{} sending PUBREL\n    {}", clientPrefix, ((MqttPubRelBuilder) pubRelBuilder).build());
    }

    @Override
    public void onPubRecError(
            final @NotNull Mqtt5ClientConfig clientConfig,
            final @NotNull Mqtt5Publish publish,
            final @NotNull Mqtt5PubRec pubRec) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} received PUBREC\n    {}", clientPrefix, pubRec);
    }

    @Override
    public void onPubComp(
            final @NotNull Mqtt5ClientConfig clientConfig,
            final @NotNull Mqtt5PubRel pubRel,
            final @NotNull Mqtt5PubComp pubComp) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} received PUBCOMP\n    {}", clientPrefix, pubComp);
    }
}
