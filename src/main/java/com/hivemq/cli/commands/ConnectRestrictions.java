package com.hivemq.cli.commands;

import org.jetbrains.annotations.Nullable;

public interface ConnectRestrictions {

    @Nullable Integer getReceiveMaximum();

    @Nullable Integer getSendMaximum();

    @Nullable Integer getMaximumPacketSize();

    @Nullable Integer getSendMaximumPacketSize();

    @Nullable Integer getTopicAliasMaximum();

    @Nullable Integer getSendTopicAliasMaximum();

    @Nullable Boolean getRequestProblemInformation();

    @Nullable Boolean getRequestResponseInformation();

}
