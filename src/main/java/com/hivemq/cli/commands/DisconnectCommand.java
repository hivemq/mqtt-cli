package com.hivemq.cli.commands;

import com.hivemq.cli.impl.MqttAction;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "dis", aliases = "disconnect", description = "Disconnects an mqtt client")
public class DisconnectCommand extends MqttCommand implements MqttAction {

    private MqttClientExecutor mqttClientExecutor;

    @Inject
    DisconnectCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {

        this.mqttClientExecutor = mqttClientExecutor;

    }

    @Override
    public Class getType() {
        return DisconnectCommand.class;
    }

    @Override
    public void run() {

        if (isVerbose()) {
            Logger.trace("Command: {} ", this);
        }

        try {
            mqttClientExecutor.disconnect(this);
        } catch (final Exception ex) {
            if (isDebug()) {
                Logger.debug(ex);
            }
            Logger.error(ex.getMessage());

        }

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
