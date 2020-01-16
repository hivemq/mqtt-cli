/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.mqtt;

import com.hivemq.cli.commands.Subscribe;
import com.hivemq.cli.utils.FileUtils;
import com.hivemq.cli.utils.LoggerUtils;
import com.hivemq.cli.utils.json.JsonMqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SubscribeMqtt3PublishCallback implements Consumer<Mqtt3Publish> {

    @Nullable private final File publishFile;
    private final boolean printToStdout;
    private final boolean isBase64;
    private final boolean isJsonOutput;
    private final Mqtt3Client client;

    SubscribeMqtt3PublishCallback(final @NotNull Subscribe subscribe, final @NotNull Mqtt3Client client) {
        printToStdout = subscribe.isPrintToSTDOUT();
        publishFile = subscribe.getPublishFile();
        isBase64 = subscribe.isBase64();
        isJsonOutput = subscribe.isJsonOutput();
        this.client = client;
    }

    @Override
    public void accept(final @NotNull Mqtt3Publish mqtt3Publish) {

        String message;

        if (isJsonOutput) {
            message = new JsonMqtt3Publish(mqtt3Publish, isBase64).toString();
        } else if (isBase64) {
            message = Base64.toBase64String(mqtt3Publish.getPayloadAsBytes());
        }
        else {
            message = new String(mqtt3Publish.getPayloadAsBytes());
        }

        if (publishFile != null) {
            PrintWriter fileWriter = FileUtils.createFileAppender(publishFile);
            fileWriter.println(mqtt3Publish.getTopic() + ": " + message);
            fileWriter.flush();
            fileWriter.close();
        }

        if (printToStdout) { System.out.println(message); }

        Logger.debug("{} received PUBLISH ('{}') {}",
                LoggerUtils.getClientPrefix(client.getConfig()),
                new String(mqtt3Publish.getPayloadAsBytes(), StandardCharsets.UTF_8),
                mqtt3Publish);
    }

}
