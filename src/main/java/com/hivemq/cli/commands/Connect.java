package com.hivemq.cli.commands;

import com.hivemq.cli.converters.*;
import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.MqttAction;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import picocli.CommandLine;

import java.nio.ByteBuffer;
import java.util.UUID;

@CommandLine.Command(name = "con", description = "Connects an mqtt client")
public class Connect extends MqttCommand implements MqttAction {

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


    //TODO Implement
    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use ssl for connection.")
    private boolean useSsl;

    //TODO REARRANGE
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

    public void setWillMessageExpiryInterval(long willMessageExpiryInterval) {
        this.willMessageExpiryInterval = willMessageExpiryInterval;
    }

    public long getWillDelayInterval() {
        return willDelayInterval;
    }

    public void setWillDelayInterval(long willDelayInterval) {
        this.willDelayInterval = willDelayInterval;
    }

    public Mqtt5PayloadFormatIndicator getWillPayloadFormatIndicator() {
        return willPayloadFormatIndicator;
    }

    public void setWillPayloadFormatIndicator(Mqtt5PayloadFormatIndicator willPayloadFormatIndicator) {
        this.willPayloadFormatIndicator = willPayloadFormatIndicator;
    }

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

    public void setWillCorrelationData(ByteBuffer willCorrelationData) {
        this.willCorrelationData = willCorrelationData;
    }

    public Mqtt5UserProperties getWillUserProperties() {
        return willUserProperties;
    }

    public void setWillUserProperties(Mqtt5UserProperties willUserProperties) {
        this.willUserProperties = willUserProperties;
    }

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
                '}';
    }

}
