package com.hivemq.cli.commands;

import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.impl.SubscriptionImpl;
import picocli.CommandLine;

import java.util.Arrays;

@CommandLine.Command(name = "sub", description = "Subscribe an mqtt client to a list of topics")
public class Subscribe extends Connect implements MqttAction {


    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "Set at least one Topic")
    private String[] topics;

    @CommandLine.Option(names = {"-q", "--qos"}, defaultValue = "0", description = "Quality of Service for the corresponding topic.")
    private int[] qos;

    /**
     * --quiet : don't print error messages.
     * --will-payload : payload for the client Will, which is sent by the broker in case of
     * unexpected disconnection. If not given and will-topic is set, a zero
     * length message will be sent.
     * --will-qos : QoS level for the client Will.
     * --will-retain : if given, make the client Will retained.
     * --will-topic : the topic on which to publish the client Will.
     * --cafile : path to a file containing trusted CA certificates to enable encrypted
     * certificate based communication.
     * --capath : path to a directory containing trusted CA certificates to enable encrypted
     * communication.
     * --cert : client certificate for authentication, if required by server.
     * --key : client private key for authentication, if required by server.
     * --ciphers : openssl compatible list of TLS ciphers to support.
     * --tls-version : TLS protocol version, can be one of tlsv1.2 tlsv1.1 or tlsv1.
     * Defaults to tlsv1.2 if available.
     * --insecure : do not check that the server certificate hostname matches the remote
     * hostname. Using this option means that you cannot be sure that the
     * remote host is the server you wish to connect to and so is insecure.
     * Do not use this option in a production environment.
     * --psk : pre-shared-key in hexadecimal (no leading 0x) to enable TLS-PSK mode.
     * --psk-identity : client identity string for TLS-PSK mode.
     * --proxy : SOCKS5 proxy URL of the form:
     * socks5h://[username[:password]@]hostname[:port]
     * Only "none" and "username" authentication is supported.
     **/


    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public int[] getQos() {
        return qos;
    }

    public void setQos(int[] qos) {
        this.qos = qos;
    }

    @Override
    public Class getType() {
        return Subscribe.class;
    }

    @Override
    public void run() {
        SubscriptionImpl.get(this).run();
    }

    public void stay() throws InterruptedException {
        SubscriptionImpl.get(this).stay();
    }

    @Override
    public String toString() {
        return "Subscribe:: {" +
                "key=" + getKey() +
                "topics=" + Arrays.toString(topics) +
                ", qos=" + Arrays.toString(qos) +
                '}';
    }


}
