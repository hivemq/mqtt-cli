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
import com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import com.hivemq.client.internal.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class Mqtt5DebugIncomingQos2Interceptor implements Mqtt5IncomingQos2Interceptor {

    @Override
    public void onPublish(
            @NotNull final Mqtt5ClientConfig clientConfig,
            @NotNull final Mqtt5Publish publish,
            @NotNull final Mqtt5PubRecBuilder pubRecBuilder) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} sending PUBREC\n    {}", clientPrefix, ((MqttPubRecBuilder) pubRecBuilder).build());
    }

    @Override
    public void onPubRel(
            @NotNull final Mqtt5ClientConfig clientConfig,
            @NotNull final Mqtt5PubRel pubRel,
            @NotNull final Mqtt5PubCompBuilder pubCompBuilder) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} received PUBREL\n    {}", clientPrefix, pubRel);
        Logger.debug("{} sending PUBCOMP\n    {}", clientPrefix, ((MqttPubCompBuilder) pubCompBuilder).build());
    }
}
