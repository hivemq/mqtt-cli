package com.hivemq.cli.utils.assertions;

import com.hivemq.extension.sdk.api.packets.disconnect.DisconnectPacket;
import org.jetbrains.annotations.NotNull;

public class DisconnectInformation {
    private final @NotNull DisconnectPacket disconnectPacket;
    private final @NotNull String clientId;

    public DisconnectInformation(final @NotNull DisconnectPacket disconnectPacket, final @NotNull String clientId) {
        this.disconnectPacket = disconnectPacket;
        this.clientId = clientId;
    }

    public @NotNull DisconnectPacket getDisconnectPacket() {
        return disconnectPacket;
    }

    public @NotNull String getClientId() {
        return clientId;
    }
}
