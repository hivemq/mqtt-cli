package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.ConnectCommand;
import com.hivemq.cli.commands.PublishCommand;
import com.hivemq.cli.commands.SubscribeCommand;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.util.encoders.Base64;

@Singleton
public class MqttClientExecutor extends AbstractMqttClientExecutor {

    @Inject
    MqttClientExecutor() {
    }

    void mqtt5Connect(final @NotNull Mqtt5BlockingClient client, final @NotNull Mqtt5Connect connectMessage, final @NotNull ConnectCommand connectCommand) {

        if (connectCommand.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("sending CONNECT with Command: {}", connectCommand);
        }

        Mqtt5ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReasonCode());
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt3Connect(final @NotNull Mqtt3BlockingClient client, final @NotNull Mqtt3Connect connectMessage, final @NotNull ConnectCommand connectCommand) {

        if (connectCommand.isDebug()) {
            Logger.debug("sending CONNECT");
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("sending CONNECT with Command: {}", connectCommand);
        }

        Mqtt3ConnAck connAck = client.connect(connectMessage);

        if (connectCommand.isDebug()) {
            Logger.debug("received CONNACK {}", connAck.getReturnCode());
        }

        if (connectCommand.isVerbose()) {
            Logger.trace("received CONNACK: {} ", connAck);
            Logger.trace("now in State: {}", client.getConfig().getState());
        }

    }

    void mqtt5Subscribe(final @NotNull Mqtt5AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (subscribeCommand.isDebug()) {
            Logger.debug("sending SUBSCRIBE: (Topic: {}, QoS: {})", topic, qos);
        }

        if (subscribeCommand.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Command: {}", subscribeCommand);
        }

        PrintWriter fileWriter = null;
        if (subscribeCommand.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribeCommand.getReceivedMessagesFile());
        }
        PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) {
                finalFileWriter.close();
            }
        }));


        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = applyBase64EncodingIfSet(subscribeCommand.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribeCommand.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribeCommand.isDebug()) {
                        Logger.debug("received PUBLISH: (Topic: {}, Message: '{}')", publish.getTopic(), payloadMessage);
                    }

                    if (subscribeCommand.isVerbose()) {
                        Logger.trace("received PUBLISH: {}", publish);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {

                    if (throwable != null) {
                        if (subscribeCommand.isDebug()) {
                            Logger.debug("SUBSCRIBE failed: {} ", topic, throwable.getStackTrace());
                        }
                        Logger.error("SUBSCRIBE failed: {}", topic, throwable.getMessage());
                    } else {

                        if (subscribeCommand.isDebug()) {
                            Logger.debug("received SUBACK: {}", subAck.getReasonCodes());
                        }

                        if (subscribeCommand.isVerbose()) {
                            Logger.trace("received SUBACK: {}", subAck);
                        }

                    }

                });
    }

    void mqtt3Subscribe(final @NotNull Mqtt3AsyncClient client, final @NotNull SubscribeCommand subscribeCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (subscribeCommand.isDebug()) {
            Logger.debug("sending SUBSCRIBE: (Topic: {}, QoS: {})", topic, qos);
        }

        if (subscribeCommand.isVerbose()) {
            Logger.trace("sending SUBSCRIBE with Command: {}", subscribeCommand);
        }

        PrintWriter fileWriter = null;
        if (subscribeCommand.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribeCommand.getReceivedMessagesFile());
        }
        PrintWriter finalFileWriter = fileWriter;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalFileWriter != null) {
                finalFileWriter.close();
            }
        }));

        client.subscribeWith()
                .topicFilter(topic)
                .qos(qos)
                .callback(publish -> {

                    byte[] payload = publish.getPayloadAsBytes();
                    final String payloadMessage = applyBase64EncodingIfSet(subscribeCommand.isBase64(), payload);

                    if (finalFileWriter != null) {
                        finalFileWriter.println(publish.getTopic() + ": " + payloadMessage);
                        finalFileWriter.flush();
                    }

                    if (subscribeCommand.isPrintToSTDOUT()) {
                        System.out.println(payloadMessage);
                    }

                    if (subscribeCommand.isDebug()) {
                        Logger.debug("received PUBLISH: (Topic: {}, Message: '{}')", publish.getTopic(), payloadMessage);
                    }

                    if (subscribeCommand.isVerbose()) {
                        Logger.trace("received PUBLISH: {}", publish);
                    }

                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {

                        if (subscribeCommand.isDebug()) {
                            Logger.debug("SUBSCRIBE failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("SUBSCRIBE failed: {}", topic, throwable.getMessage());
                    } else {

                        if (subscribeCommand.isDebug()) {
                            Logger.debug("received SUBACK: {}", subAck.getReturnCodes());
                        }

                        if (subscribeCommand.isVerbose()) {
                            Logger.trace("received SUBACK: {}", subAck);
                        }

                    }
                });
    }

    void mqtt5Publish(final @NotNull Mqtt5AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publishCommand.isDebug()) {
            Logger.debug("sending PUBLISH: (Topic: {}, QoS {},Message: '{}')", topic, qos, bufferToString(publishCommand.getMessage()));
        }

        if (publishCommand.isVerbose()) {
            Logger.trace("sending PUBLISH with command: {}", publishCommand);
        }

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {

                        if (publishCommand.isDebug()) {
                            Logger.debug("PUBLISH failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("PUBLISH failed", topic, throwable.getMessage());

                    } else {

                        final String p = bufferToString(publishCommand.getMessage());

                        if (publishCommand.isDebug()) {
                            Logger.debug("received RESULT: '{}' for PUBLISH to Topic:  {}", trimMessage(p), topic);
                        }

                        if (publishCommand.isVerbose()) {
                            Logger.trace("received RESULT: '{}' for PUBLISH to Topic: {}", publishResult, topic);
                        }

                    }
                });
    }


    void mqtt3Publish(final @NotNull Mqtt3AsyncClient client, final @NotNull PublishCommand publishCommand, final @NotNull String topic, final @NotNull MqttQos qos) {

        if (publishCommand.isDebug()) {
            Logger.debug("sending PUBLISH: (Topic: {}, QoS {},Message: '{}')", topic, qos, bufferToString(publishCommand.getMessage()));
        }

        if (publishCommand.isVerbose()) {
            Logger.trace("sending PUBLISH with command: {}", publishCommand);
        }

        client.publishWith()
                .topic(topic)
                .qos(qos)
                .retain(publishCommand.isRetain())
                .payload(publishCommand.getMessage())
                .send()
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {

                        if (publishCommand.isDebug()) {
                            Logger.debug("PUBLISH failed: {} ", topic, throwable.getStackTrace());
                        }

                        Logger.error("PUBLISH failed", topic, throwable.getMessage());

                    } else {

                        final String p = bufferToString(publishCommand.getMessage());

                        if (publishCommand.isDebug()) {
                            Logger.debug("received RESULT: '{}' for PUBLISH to Topic:  {}", trimMessage(p), topic);
                        }

                        if (publishCommand.isVerbose()) {
                            Logger.trace("received RESULT: '{}' for PUBLISH to Topic: {}", publishResult, topic);
                        }

                    }
                });
    }


    private @NotNull String applyBase64EncodingIfSet(final boolean encode, final byte[] payload) {
        if (encode) {
            return Base64.toBase64String(payload);
        } else {
            return new String(payload);
        }
    }

    private @NotNull String trimMessage(String p) {
        if (p.length() > 10) {
            return p.substring(0, 10);
        } else {
            return p;
        }

    }

    private @NotNull String bufferToString(ByteBuffer b) {
        return new String(b.array(), StandardCharsets.UTF_8);
    }
}
