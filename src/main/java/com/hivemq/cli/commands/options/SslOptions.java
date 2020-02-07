package com.hivemq.cli.commands.options;

import com.hivemq.cli.converters.DirectoryToCertificateCollectionConverter;
import com.hivemq.cli.converters.FileToCertificateConverter;
import com.hivemq.cli.converters.FileToPrivateKeyConverter;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class SslOptions {
    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use default ssl configuration if no other ssl options are specified (default: false)", order = 2)
    private boolean useSsl;

    @CommandLine.Option(names = {"--cafile"}, paramLabel = "FILE", converter = FileToCertificateConverter.class, description = "Path to a file containing trusted CA certificates to enable encrypted certificate based communication", order = 2)
    @Nullable
    private Collection<X509Certificate> certificates;

    @CommandLine.Option(names = {"--capath"}, paramLabel = "DIR", converter = DirectoryToCertificateCollectionConverter.class, description = {"Path to a directory containing certificate files to import to enable encrypted certificate based communication"}, order = 2)
    @Nullable
    private Collection<X509Certificate> certificatesFromDir;

    @CommandLine.Option(names = {"--ciphers"}, split = ":", description = "The client supported cipher suites list in IANA format separated with ':'", order = 2)
    @Nullable
    private Collection<String> cipherSuites;

    @CommandLine.Option(names = {"--tls-version"}, description = "The TLS protocol version to use (default: {'TLSv.1.2'})", order = 2)
    @Nullable
    private Collection<String> supportedTLSVersions;

    @CommandLine.Option(names = {"--cert"}, converter = FileToCertificateConverter.class, description = "The client certificate to use for client side authentication", order = 2)
    @Nullable
    private X509Certificate clientCertificate;

    @CommandLine.Option(names = {"--key"}, converter = FileToPrivateKeyConverter.class, description = "The path to the client private key for client side authentication", order = 2)
    @Nullable
    private PrivateKey clientPrivateKey;
}
