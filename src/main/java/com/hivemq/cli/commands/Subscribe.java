package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface Subscribe extends Context {

    public String[] getTopics();

    public void setTopics(final String[] topics);

    public MqttQos[] getQos();

    public void setQos(final MqttQos[] qos);

    public File getReceivedMessagesFile();

    public void setReceivedMessagesFile(@Nullable final File receivedMessagesFile);

    public boolean isPrintToSTDOUT();

    public void setPrintToSTDOUT(final boolean printToSTDOUT);

    public boolean isBase64();

    public void setBase64(final boolean base64);
}
