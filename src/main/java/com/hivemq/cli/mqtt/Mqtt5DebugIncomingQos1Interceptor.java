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
