package com.hivemq.cli.mqtt.test;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.nio.ByteBuffer;

public class Mqtt5FeatureTester {

    private int maxTopicLength = -1;
    private final String host;
    private final int port;
    private final String username;
    private final ByteBuffer password;
    private final MqttClientSslConfig sslConfig;

    public Mqtt5FeatureTester(final @NotNull String host,
                              final @NotNull Integer port,
                              final @Nullable String username,
                              final @Nullable ByteBuffer password,
                              final @Nullable MqttClientSslConfig sslConfig) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.sslConfig = sslConfig;
    }

    // Tests

    public @Nullable Mqtt5ConnAck testConnect() {
        final Mqtt5Client mqtt5Client = buildClient();

        try {
            return mqtt5Client.toBlocking().connect();
        }
        catch (final Mqtt5ConnAckException connAckEx) { return connAckEx.getMqttMessage(); }
        catch (final Exception ex) {
            Logger.error(ex, "Could not connect MQTT5 client");
            return null;
        }
        finally {
            disconnectIfNotConnected(mqtt5Client);
        }
    }





    // Helpers

    private @NotNull Mqtt5Client buildClient() {
        return getClientBuilder()
                .build();
    }

    private @NotNull Mqtt5ClientBuilder getClientBuilder() {
        final Mqtt5ClientBuilder mqtt5ClientBuilder = Mqtt5Client.builder()
                .serverHost(host)
                .serverPort(port)
                .simpleAuth(buildAuth());

        if (sslConfig != null) { mqtt5ClientBuilder.sslConfig(sslConfig); }

        return mqtt5ClientBuilder;
    }

    private @Nullable Mqtt5SimpleAuth buildAuth() {
        if (username != null && password != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(username)
                    .password(password)
                    .build();
        }
        else if (username != null) {
            return Mqtt5SimpleAuth.builder()
                    .username(username)
                    .build();
        }
        else if (password != null) {
            return Mqtt5SimpleAuth.builder()
                    .password(password)
                    .build();
        }
        else {
            return null;
        }
    }

    private void disconnectIfNotConnected(final @NotNull Mqtt5Client ... clients) {
        for (Mqtt5Client client: clients) {
            if (client.getState().isConnected()) {
                client.toBlocking().disconnect();
            }
        }
    }
}
