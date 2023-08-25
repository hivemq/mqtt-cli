package com.hivemq.cli.mqtt;

import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class Mqtt5DebugOutgoingQos1Interceptor implements Mqtt5OutgoingQos1Interceptor {

    @Override
    public void onPubAck(
            final @NotNull Mqtt5ClientConfig clientConfig,
            final @NotNull Mqtt5Publish publish,
            final @NotNull Mqtt5PubAck pubAck) {
        final String clientPrefix = LoggerUtils.getClientPrefix(clientConfig);
        Logger.debug("{} received PUBACK\n    {}", clientPrefix, pubAck);
    }
}
