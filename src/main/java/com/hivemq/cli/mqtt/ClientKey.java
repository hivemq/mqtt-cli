package com.hivemq.cli.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ClientKey {

    private final @Nullable String clientIdentifier;
    private final @NotNull String hostname;

    private ClientKey(final @Nullable String clientIdentifier, final @NotNull String hostname) {
        this.clientIdentifier = clientIdentifier;
        this.hostname = hostname;
    }

    public static ClientKey of(final @Nullable String clientIdentifier, final @NotNull String hostname) {
        return new ClientKey(clientIdentifier, hostname);
    }

    public static ClientKey of(final @NotNull MqttClient client) {
        return new ClientKey(client.getConfig().getClientIdentifier().map(Objects::toString).orElse(""),
                client.getConfig().getServerHost());
    }


    @Override
    public String toString() {
        return "Client{" + "clientIdentifier='" + clientIdentifier + '\'' + ", hostname='" + hostname + '\'' + '}';
    }
}
