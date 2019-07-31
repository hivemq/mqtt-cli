package com.hivemq.cli.commands;

import com.hivemq.cli.converters.MqttQosConverter;
import com.hivemq.cli.converters.UnsignedIntConverter;
import com.hivemq.cli.converters.UnsignedShortConverter;
import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command(name = "con", description = "Connects an mqtt client")
public class Connect extends MqttCommand implements MqttAction {

    //TODO Implement
    @CommandLine.Option(names = {"-pi", "--prefixIdentifier"}, description = "The prefix of the client Identifier UTF-8 String.")
    private String prefixIdentifier;

    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for the client UTF-8 String.")
    private String user;

    @CommandLine.Option(names = {"-pw", "--password"}, description = "The password for the client UTF-8 String.")
    private byte[] password;

    @CommandLine.Option(names = {"-k", "--keepAlive"}, converter = UnsignedShortConverter.class, defaultValue = "60", description = "A keep alive of the client (in seconds).")
    private int keepAlive;

    @CommandLine.Option(names = {"-c", "--cleanStart"}, defaultValue = "true", description = "Define a clean start for the connection.")
    private boolean cleanStart;

    @CommandLine.Option(names = {"-wm", "--willMessage"}, defaultValue = "", description = "The payload of the will message.")
    private byte[] willMessage;

    @CommandLine.Option(names = {"-wq", "--willQualityOfService"}, converter = MqttQosConverter.class, defaultValue = "AT_MOST_ONCE", description = "Quality of Service for the will message.")
    private MqttQos willQos;

    @CommandLine.Option(names = {"-wr", "--willRetain"}, defaultValue = "false", description = "Will message as retained message")
    private boolean willRetain;

    @CommandLine.Option(names = {"-wt", "--willTopic"}, description = "The topic of the will message.")
    private String willTopic;


    // TODO
    @CommandLine.Option(names = {"-we", "--willMessageExpiryInterval"}, converter = UnsignedIntConverter.class, defaultValue = "4294967295", description = "The lifetime of the Will Message in seconds.")
    private long willMessageExpiryInterval;

    @CommandLine.Option(names = {"-wd", "--willDelayInterval"}, converter = UnsignedIntConverter.class, defaultValue = "0", description = "The Server delays publishing the Client's Will Message until the Will Delay has passed.")
    private long willDelayInterval;

    @CommandLine.Option(names = {"-wp", "--willPayloadFormatIndicator"}, defaultValue = "false", description = "The Payload Format Indicator.")
    private boolean willPayloadFormatIndicator;

    @CommandLine.Option(names = {"-wc", "--willContentType"}, description = "A description of Will Message's content.")
    private String willContentType;

    @CommandLine.Option(names = {"-wrt", "--willResponseTopic"}, description = "The Topic Name for a response message.")
    private String willResponseTopic;

    @CommandLine.Option(names = {"-wcd", "--willCorrelationData"}, description = "The Correlation Data of the Will Message.")
    private String willCorrelationData;

    @CommandLine.Option(names = {"-wu", "--willUserProperties"}, arity = "2", description = "The User Property of the Will Message.")
    private String[] willUserProperties;


    //TODO Implement
    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use ssl for connection.")
    private boolean useSsl;

    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, defaultValue = "0", converter = UnsignedIntConverter.class, description = "Session expiry can be disabled by setting it to 4_294_967_295")
    private long sessionExpiryInterval;

    public String createIdentifier() {
        if (getIdentifier() == null) {
            this.setIdentifier("hmqClient" + this.getVersion() + "-" + UUID.randomUUID().toString());
        }
        return getIdentifier();
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String getPrefixIdentifier() {
        return prefixIdentifier;
    }

    public String getUser() {
        return user;
    }

    public byte[] getPassword() {
        return password;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(byte[] willMessage) {
        this.willMessage = willMessage;
    }

    public MqttQos getWillQos() {
        return willQos;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public long getSessionExpiryInterval() { return sessionExpiryInterval; }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public void setPrefixIdentifier(String prefixIdentifier) {
        this.prefixIdentifier = prefixIdentifier;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setWillQos(MqttQos willQos) {
        this.willQos = willQos;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setCleanStart(boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public void setSessionExpiryInterval(long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
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

    @Override
    public void run() {
        ConnectionImpl.get(this).run();
    }

    @Override
    public String toString() {
        return "Connect{" +
                "key=" + getKey() +
                ", prefixIdentifier='" + prefixIdentifier + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", keepAlive=" + keepAlive +
                ", cleanStart=" + cleanStart +
                ", willMessage='" + willMessage + '\'' +
                ", willQos=" + willQos +
                ", willRetain=" + willRetain +
                ", willTopic='" + willTopic + '\'' +
                ", useSsl=" + useSsl +
                ", sessionExpiryInterval=" + sessionExpiryInterval + '\'' +
                '}';
    }

}
