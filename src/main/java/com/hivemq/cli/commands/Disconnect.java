package com.hivemq.cli.commands;

import com.hivemq.cli.impl.DisconnectImpl;
import com.hivemq.cli.impl.MqttAction;
import picocli.CommandLine;

@CommandLine.Command(name = "dis", description = "Disconnects an mqtt client")
public class Disconnect extends MqttCommand implements MqttAction {

    @Override
    public Class getType() {
        return Disconnect.class;
    }

    @Override
    public void run() {
        DisconnectImpl.get(this).run();

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
    public String toString() {
        return "Disconnect::" + getKey();
    }


}
