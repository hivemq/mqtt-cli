package com.hivemq.cli.commands;

import com.hivemq.cli.converters.*;
import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@CommandLine.Command(name = "con", description = "Connects an mqtt client")
public class Connect extends MqttCommand implements MqttAction {

    private static final String DEFAULT_TLS_VERSION = "TLSv1.2";

    private MqttClientSslConfig sslConfig;

    private List<X509Certificate> certificates = new ArrayList<>();

    //TODO Implement
    @CommandLine.Option(names = {"-pi", "--prefixIdentifier"}, description = "The prefix of the client Identifier UTF-8 String.")
    private String prefixIdentifier;

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for the client UTF-8 String.")
    private String user;

    @CommandLine.Option(names = {"-pw", "--password"}, converter = ByteBufferConverter.class, description = "The password for the client UTF-8 String.")
    private ByteBuffer password;

    @CommandLine.Option(names = {"-k", "--keepAlive"}, converter = UnsignedShortConverter.class, defaultValue = "60", description = "A keep alive of the client (in seconds).")
    private int keepAlive;

    @CommandLine.Option(names = {"-c", "--cleanStart"}, defaultValue = "true", description = "Define a clean start for the connection.")
    private boolean cleanStart;

    @CommandLine.Option(names = {"-wt", "--willTopic"}, description = "The topic of the will message.")
    private String willTopic;

    @CommandLine.Option(names = {"-wm", "--willMessage"}, converter = ByteBufferConverter.class, description = "The payload of the will message.")
    private ByteBuffer willMessage;

    @CommandLine.Option(names = {"-wq", "--willQualityOfService"}, converter = MqttQosConverter.class, defaultValue = "AT_MOST_ONCE", description = "Quality of Service for the will message.")
    private MqttQos willQos;

    @CommandLine.Option(names = {"-wr", "--willRetain"}, defaultValue = "false", description = "Will message as retained message")
    private boolean willRetain;

    @CommandLine.Option(names = {"-we", "--willMessageExpiryInterval"}, converter = UnsignedIntConverter.class, defaultValue = "4294967295", description = "The lifetime of the Will Message in seconds.")
    private long willMessageExpiryInterval;

    @CommandLine.Option(names = {"-wd", "--willDelayInterval"}, converter = UnsignedIntConverter.class, defaultValue = "0", description = "The Server delays publishing the Client's Will Message until the Will Delay has passed.")
    private long willDelayInterval;

    @CommandLine.Option(names = {"-wp", "--willPayloadFormatIndicator"}, converter = PayloadFormatIndicatorConverter.class, description = "The Payload Format Indicator.")
    private Mqtt5PayloadFormatIndicator willPayloadFormatIndicator;

    @CommandLine.Option(names = {"-wc", "--willContentType"}, description = "A description of Will Message's content.")
    private String willContentType;

    @CommandLine.Option(names = {"-wrt", "--willResponseTopic"}, description = "The Topic Name for a response message.")
    private String willResponseTopic;

    @CommandLine.Option(names = {"-wcd", "--willCorrelationData"}, converter = ByteBufferConverter.class, description = "The Correlation Data of the Will Message.")
    private ByteBuffer willCorrelationData;

    @CommandLine.Option(names = {"-wu", "--willUserProperties"}, converter = UserPropertiesConverter.class, description = "The User Property of the Will Message. Usage: Key=Value, Key1=Value1|Key2=Value2")
    private Mqtt5UserProperties willUserProperties;

    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, defaultValue = "0", converter = UnsignedIntConverter.class, description = "Session expiry can be disabled by setting it to 4_294_967_295")
    private long sessionExpiryInterval;

    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use default ssl configuration if no other ssl options are specified.")
    private boolean useSsl;

    @CommandLine.Option(names = {"--cafile"}, converter = FileToCertificateConverter.class, description = "Path to a file containing trusted CA certificates to enable encrypted certificate based communication.")
    private void addCAFile(X509Certificate certificate) {
        certificates.add(certificate);
    }

    @CommandLine.Option(names = {"--capath"}, converter = DirectoryToCertificateCollectionConverter.class, description = {"Path to a directory containing certificate files to import to enable encrypted certificate based communication."})
    private void addCACollection(Collection<X509Certificate> certs) {
        certificates.addAll(certs);
    }

    @CommandLine.Option(names = {"--ciphers"}, split = ":", description = "The client supported cipher suites list generated with 'openssl ciphers'.")
    private Collection<String> cipherSuites;

    @CommandLine.Option(names = {"--tls-version"}, description = "The TLS protocol version to use.")
    private Collection<String> supportedTLSVersions;

    @CommandLine.ArgGroup(exclusive = false)
    private ClientSideAuthentication clientSideAuthentication;

    static class ClientSideAuthentication {

        @CommandLine.Option(names = {"--cert"}, required = true, converter = FileToCertificateConverter.class, description = "The Client certificate to use for client-side authentication.")
        X509Certificate clientCertificate;

        @CommandLine.Option(names = {"--key"}, required = true, converter = FileToPrivateKeyConverter.class, description = "The path to the client private key for client side authentication.")
        PrivateKey clientPrivateKey;
    }

    @Override
    public void run() {

        if (useBuiltSslConfig()) {
            try {
                buildSslConfig();
            } catch (Exception e) {
                Logger.debug(e);
            }
        }

        ConnectionImpl.get(this).run();
    }

    private boolean useBuiltSslConfig() {
        return !certificates.isEmpty() ||
                cipherSuites != null ||
                supportedTLSVersions != null ||
                clientSideAuthentication != null ||
                useSsl;
    }

    @Override
    public Class getType() {
        return Connect.class;
    }

    @Override
    public String getKey() {
        return "client {" +
                "version=" + getVersion() +
                ", host='" + getHost() + '\'' +
                ", port=" + getPort() +
                ", identifier='" + getIdentifier() + '\'' +
                '}';
    }

    private void buildSslConfig() throws Exception {
        // use ssl Port if the user forgot to set it
        if (getPort() == MqttClient.DEFAULT_SERVER_PORT) setPort(MqttClient.DEFAULT_SERVER_PORT_SSL);

        // build trustManagerFactory for server side authentication and to enable tls
        TrustManagerFactory trustManagerFactory = null;
        if (!certificates.isEmpty()) {
            trustManagerFactory = buildTrustManagerFactory(certificates);
        }


        // build keyManagerFactory if clientSideAuthentication is used
        KeyManagerFactory keyManagerFactory = null;
        if (clientSideAuthentication != null) {
            keyManagerFactory = buildKeyManagerFactory(clientSideAuthentication.clientCertificate, clientSideAuthentication.clientPrivateKey);
        }

        // default to tlsv.2
        if (supportedTLSVersions == null) {
            supportedTLSVersions = new ArrayList<>();
            supportedTLSVersions.add(DEFAULT_TLS_VERSION);
        }

        sslConfig = MqttClientSslConfig.builder()
                .trustManagerFactory(trustManagerFactory)
                .keyManagerFactory(keyManagerFactory)
                .cipherSuites(cipherSuites)
                .protocols(supportedTLSVersions)
                .build();
    }

    public String createIdentifier() {
        if (getIdentifier() == null) {
            this.setIdentifier("hmqClient" + this.getVersion() + "-" + UUID.randomUUID().toString());
        }
        return getIdentifier();
    }


    private TrustManagerFactory buildTrustManagerFactory(final @NotNull Collection<X509Certificate> certCollection) throws Exception {

        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);

        // add all certificates of the collection to the KeyStore
        int i = 1;
        for (final X509Certificate cert : certCollection) {
            String alias = Integer.toString(i);
            ks.setCertificateEntry(alias, cert);
            i++;
        }

        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        trustManagerFactory.init(ks);

        return trustManagerFactory;
    }

    private KeyManagerFactory buildKeyManagerFactory(final @NotNull X509Certificate cert, final @NotNull PrivateKey key) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {

        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        ks.load(null, null);

        final Certificate[] certChain = new Certificate[1];
        certChain[0] = cert;
        ks.setKeyEntry("mykey", key, null, certChain);

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(ks, null);

        return keyManagerFactory;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    @Override
    public String toString() {
        return "Connect{" +
                "key=" + getKey() +
                ", prefixIdentifier='" + prefixIdentifier + '\'' +
                ", user='" + user + '\'' +
                ", password=" + password +
                ", keepAlive=" + keepAlive +
                ", cleanStart=" + cleanStart +
                ", willTopic='" + willTopic + '\'' +
                ", willQos=" + willQos +
                ", willMessage='" + willMessage + '\'' +
                ", willRetain=" + willRetain +
                ", willMessageExpiryInterval=" + willMessageExpiryInterval +
                ", willDelayInterval=" + willDelayInterval +
                ", willPayloadFormatIndicator=" + willPayloadFormatIndicator +
                ", willContentType='" + willContentType + '\'' +
                ", willResponseTopic='" + willResponseTopic + '\'' +
                ", willCorrelationData=" + willCorrelationData +
                ", willUserProperties=" + willUserProperties +
                ", useSsl=" + useSsl +
                ", sessionExpiryInterval=" + sessionExpiryInterval +
                ", sslConfig=" + sslConfig +
                '}';
    }


    // GETTER AND SETTER

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public String getPrefixIdentifier() {
        return prefixIdentifier;
    }

    public void setPrefixIdentifier(String prefixIdentifier) {
        this.prefixIdentifier = prefixIdentifier;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ByteBuffer getPassword() {
        return password;
    }

    public void setPassword(ByteBuffer password) {
        this.password = password;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public void setCleanStart(boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public long getWillMessageExpiryInterval() {
        return willMessageExpiryInterval;
    }

    public void setWillMessageExpiryInterval(long willMessageExpiryInterval) { this.willMessageExpiryInterval = willMessageExpiryInterval; }

    public long getWillDelayInterval() {
        return willDelayInterval;
    }

    public void setWillDelayInterval(long willDelayInterval) {
        this.willDelayInterval = willDelayInterval;
    }

    public Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator() {
        return willPayloadFormatIndicator;
    }

    public void setWillPayloadFormatIndicator(Mqtt5PayloadFormatIndicator willPayloadFormatIndicator) { this.willPayloadFormatIndicator = willPayloadFormatIndicator; }

    public String getWillContentType() {
        return willContentType;
    }

    public void setWillContentType(String willContentType) {
        this.willContentType = willContentType;
    }

    public String getWillResponseTopic() {
        return willResponseTopic;
    }

    public void setWillResponseTopic(String willResponseTopic) {
        this.willResponseTopic = willResponseTopic;
    }

    public ByteBuffer getWillCorrelationData() {
        return willCorrelationData;
    }

    public void setWillCorrelationData(ByteBuffer willCorrelationData) { this.willCorrelationData = willCorrelationData; }

    public Mqtt5UserProperties getWillUserProperties() {
        return willUserProperties;
    }

    public void setWillUserProperties(Mqtt5UserProperties willUserProperties) { this.willUserProperties = willUserProperties; }

    public ByteBuffer getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(ByteBuffer willMessage) {
        this.willMessage = willMessage;
    }

    public MqttQos getWillQos() {
        return willQos;
    }

    public void setWillQos(MqttQos willQos) {
        this.willQos = willQos;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(MqttClientSslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

}
