package com.hivemq.cli.utils.broker;

import org.jetbrains.annotations.NotNull;

public enum TlsVersion {
    TLS_1_3("TLSv1.3"),
    TLS_1_2("TLSv1.2");
    //TLS_1_1("TLSv1.1"),
    //TLS_1_0("TLSv1"),
    //SSL_3_0("SSLv3");

    private final @NotNull String tlsString;

    @Override
    public @NotNull String toString() {
        return tlsString;
    }

    TlsVersion(final @NotNull String asString) {
        tlsString = asString;
    }
}
