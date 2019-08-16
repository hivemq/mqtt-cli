package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;
import org.pmw.tinylog.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.util.Base64;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    boolean mqttConnect(final @NotNull Mqtt5BlockingClient client, Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand) {
        Mqtt5ConnAck connAck = client.connect(connectMessage);
        if (connectCommand.isDebug()) {
            Log.debug("Client connect with {} ", connectCommand.toString());
        } else {
            Logger.info("Client connect with {} ", connAck.getReasonCode());
        }
        return client.getConfig().getState().isConnected();
    }

    void mqttSubscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final String topic, final MqttQos qos) {

        PrintWriter fileWriter = null;
        if (subscribeCommand.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribeCommand.getReceivedMessagesFile());
        }
        PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) finalFileWriter.close();
        }));


        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = subscribeCommand.isBase64() ? new String(Base64.getEncoder().encode(payload)) : new String(payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribeCommand.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribeCommand.isDebug()) {
                        Log.debug("Client received on topic: {} message: '{}' ", topic, payloadMessage);
                    } else {
                        Logger.info("Client received msg: '{}...' ", payloadMessage.length() > 10 ? payloadMessage.substring(0, 10) : payloadMessage);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Log.debug("Client subscribe failed with reason: {} ", topic, throwable.getStackTrace());
                        } else {
                            Logger.error("Client subscribe failed with reason: {}", topic, throwable.getMessage());
                        }

                    } else {
                        Logger.info("Client subscribed to Topic: {} ", topic);
                    }
                });
    }
}
