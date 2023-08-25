/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.options.SubscribeOptions;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.MqttPublishUtils;
import com.hivemq.cli.utils.json.JsonMqttPublish;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SubscribeMqtt3PublishCallback implements Consumer<Mqtt3Publish> {

    private final @Nullable File outputFile;
    private final @NotNull Mqtt3Client client;
    private final boolean printToStdout;
    private final boolean isBase64;
    private final boolean isJsonOutput;
    private final boolean showTopics;

    SubscribeMqtt3PublishCallback(final @NotNull SubscribeOptions subscribeOptions, final @NotNull Mqtt3Client client) {
        printToStdout = subscribeOptions.isPrintToSTDOUT();
        outputFile = subscribeOptions.getOutputFile();
        isBase64 = subscribeOptions.isBase64();
        isJsonOutput = subscribeOptions.isJsonOutput();
        showTopics = subscribeOptions.isShowTopics();
        this.client = client;
    }

    @Override
    public void accept(final @NotNull Mqtt3Publish mqtt3Publish) {
        String message;
        try {
            if (isJsonOutput) {
                message = new JsonMqttPublish(mqtt3Publish, isBase64).toString();
            } else {
                message = MqttPublishUtils.formatPayload(mqtt3Publish.getPayloadAsBytes(), isBase64);
            }

            if (showTopics) {
                message = mqtt3Publish.getTopic() + ": " + message;
            }

            Logger.debug("{} received PUBLISH ('{}')\n    {}",
                    LoggerUtils.getClientPrefix(client.getConfig()),
                    new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8),
                    mqtt3Publish);
        } catch (final Exception e) {
            Logger.error("An error occurred while processing an incoming PUBLISH.", e);
            return;
        }

        if (outputFile != null) {
            MqttPublishUtils.printToFile(outputFile, message);
        }

        if (printToStdout) {
            if (System.out.checkError()) {
                //TODO: Handle SIGPIPE
                //throw new RuntimeException("SIGNAL RECEIVED. PIPE CLOSED");
            }
            System.out.println(message);
        }
    }
}
