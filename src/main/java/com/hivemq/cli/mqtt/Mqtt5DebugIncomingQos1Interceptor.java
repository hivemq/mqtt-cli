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
import com.hivemq.client.internal.mqtt.message.publish.puback.MqttPubAckBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class Mqtt5DebugIncomingQos1Interceptor implements Mqtt5IncomingQos1Interceptor {

    @Override
    public void onPublish(
            final @NotNull Mqtt5ClientConfig clientConfig,
            final @NotNull Mqtt5Publish publish,
            final @NotNull Mqtt5PubAckBuilder pubAckBuilder) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} sending PUBACK\n    {}", clientPrefix, ((MqttPubAckBuilder) pubAckBuilder).build());
    }
}
