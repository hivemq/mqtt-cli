package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.Nullable;

public interface Disconnect extends Context {


    @Nullable Long getSessionExpiryInterval();

    @Nullable String getReasonString();

    @Nullable Mqtt5UserProperties getUserProperties();

}
