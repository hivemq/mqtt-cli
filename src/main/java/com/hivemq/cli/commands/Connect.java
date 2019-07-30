package com.hivemq.cli.commands;

import com.hivemq.cli.impl.ConnectionImpl;
import com.hivemq.cli.impl.MqttAction;
import picocli.CommandLine;

import java.util.UUID;

@CommandLine.Command(name = "con", description = "Connects an mqtt client")
public class Connect extends MqttCommand implements MqttAction {

    //TODO Implement
    @CommandLine.Option(names = {"-pi", "--prefixIdentifier"}, description = "The prefix of the client Identifier UTF-8 String.")
    private String prefixIdentifier;

    //TODO Implement
    @CommandLine.Option(names = {"-u", "--user"}, description = "The username for the client UTF-8 String.")
    private String user;

    //TODO Implement
    @CommandLine.Option(names = {"-pw", "--password"}, description = "The password for the client UTF-8 String.")
    private String password;

    //TODO Implement
    @CommandLine.Option(names = {"-k", "--keepAlive"}, defaultValue = "60", description = "A keep alive of the client (in seconds).")
    private int keepAlive;

    //TODO Implement
    @CommandLine.Option(names = {"-c", "--cleanStart"}, defaultValue = "true", description = "Define a clean start for the connection.")
    private boolean cleanStart;

    @CommandLine.Option(names = {"-wm", "--willMessage"}, defaultValue = "", description = "The payload of the will message.")
    private String willMessage;

    @CommandLine.Option(names = {"-wq", "--willQualityOfService"}, defaultValue = "0", description = "Quality of Service for the will message.")
    private int willQos;

    @CommandLine.Option(names = {"-wr", "--willRetain"}, defaultValue = "false", description = "Will message as retained message")
    private boolean willRetain;

    @CommandLine.Option(names = {"-wt", "--willTopic"}, description = "The topic of the will message.")
    private String willTopic;

    //TODO Implement
    @CommandLine.Option(names = {"-s", "--secure"}, defaultValue = "false", description = "Use ssl for connection.")
    private boolean useSsl;

    @CommandLine.Option(names = {"-se", "--sessionExpiryInterval"}, defaultValue = "0", description = "Session expiry can be disabled by setting it to 4294967295")
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

    public String getPassword() {
        return password;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isCleanStart() {
        return cleanStart;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }

    public int getWillQos() {
        return willQos;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
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
