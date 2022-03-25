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

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.MqttPublishUtils;
import com.hivemq.cli.utils.json.JsonMqttPublish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SubscribeMqtt5PublishCallback implements Consumer<Mqtt5Publish> {

    private final @Nullable File outputFile;
    private final @NotNull Mqtt5Client client;
    private final boolean printToStdout;
    private final boolean isBase64;
    private final boolean isJsonOutput;
    private final boolean showTopics;

    SubscribeMqtt5PublishCallback(final @NotNull Subscribe subscribe, final @NotNull Mqtt5Client client) {
        printToStdout = subscribe.isPrintToSTDOUT();
        outputFile = subscribe.getOutputFile();
        isBase64 = subscribe.isBase64();
        isJsonOutput = subscribe.isJsonOutput();
        showTopics = subscribe.showTopics();
        this.client = client;
    }

    @Override
    public void accept(final @NotNull Mqtt5Publish mqtt5Publish) {
        String message;

        if (isJsonOutput) {
            message = new JsonMqttPublish(mqtt5Publish, isBase64).toString();
        } else {
            message = MqttPublishUtils.formatPayload(mqtt5Publish.getPayloadAsBytes(), isBase64);
        }

        if (showTopics) {
            message = mqtt5Publish.getTopic() + ": " + message;
        }

        if (outputFile != null) {
            MqttPublishUtils.printToFile(outputFile, message);
        }
        if (printToStdout) {
            System.out.println(message);
        }

        Logger.debug(
                "{} received PUBLISH ('{}') {}",
                LoggerUtils.getClientPrefix(client.getConfig()),
                new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8),
                mqtt5Publish);
    }
}
