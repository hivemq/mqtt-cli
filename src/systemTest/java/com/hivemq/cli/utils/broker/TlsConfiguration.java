package com.hivemq.cli.utils.broker;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TlsConfiguration {

    private final boolean tlsEnabled;
    private final @NotNull List<TlsVersion> tlsVersions;
    private final @NotNull List<String> cipherSuites;
    private final boolean clientAuthentication;

    public TlsConfiguration(
            final boolean tlsEnabled,
            final @NotNull List<TlsVersion> tlsVersions,
            final @NotNull List<String> cipherSuites,
            final boolean clientAuthentication) {
        this.tlsEnabled = tlsEnabled;
        this.tlsVersions = tlsVersions;
        this.cipherSuites = cipherSuites;
        this.clientAuthentication = clientAuthentication;
    }

    public static Builder builder() {
        return new TlsConfiguration.Builder();
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public @NotNull List<TlsVersion> getTlsVersions() {
        return tlsVersions;
    }

    public @NotNull List<String> getCipherSuites() {
        return cipherSuites;
    }

    public boolean isClientAuthentication() {
        return clientAuthentication;
    }


    public static class Builder {

        private boolean tlsEnabled = false;
        private @NotNull List<TlsVersion> tlsVersions = List.of();
        private @NotNull List<String> cipherSuites = List.of();
        private boolean clientAuthentication = false;


        public @NotNull Builder withTlsEnabled(final boolean tlsEnabled) {
            this.tlsEnabled = tlsEnabled;
            return this;
        }

        public @NotNull Builder withTlsVersions(final @NotNull List<TlsVersion> tlsVersions) {
            this.tlsVersions = tlsVersions;
            return this;
        }


        public @NotNull Builder withCipherSuites(final @NotNull List<String> cipherSuites) {
            this.cipherSuites = cipherSuites;
            return this;
        }

        public @NotNull Builder withClientAuthentication(final boolean clientAuthentication) {
            this.clientAuthentication = clientAuthentication;
            return this;
        }

        public @NotNull TlsConfiguration build() {
            return new TlsConfiguration(tlsEnabled, tlsVersions, cipherSuites, clientAuthentication);
        }
    }
}


