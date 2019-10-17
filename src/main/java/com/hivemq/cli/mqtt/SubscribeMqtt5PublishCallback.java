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
import com.hivemq.cli.utils.SubscribeMqttPublishCallbackUtils;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SubscribeMqtt5PublishCallback implements Consumer<Mqtt5Publish> {

    @NotNull private final Subscribe subscribe;

    SubscribeMqtt5PublishCallback(final @NotNull Subscribe subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public void accept(final @NotNull Mqtt5Publish mqtt5Publish) {

        PrintWriter fileWriter = null;
        if (subscribe.getReceivedMessagesFile() != null) {
            fileWriter = FileUtils.createFileAppender(subscribe.getReceivedMessagesFile());
        }


        byte[] payload = mqtt5Publish.getPayloadAsBytes();
        final String payloadMessage = SubscribeMqttPublishCallbackUtils.applyBase64EncodingIfSet(subscribe.isBase64(), payload);

        if (fileWriter != null) {
            fileWriter.println(mqtt5Publish.getTopic() + ": " + payloadMessage);
            fileWriter.flush();
            fileWriter.close();
        }

        if (subscribe.isPrintToSTDOUT()) {
            System.out.println(payloadMessage);
        }

        if (subscribe.isVerbose()) {
            Logger.trace("received PUBLISH: {}, MESSAGE: '{}'", mqtt5Publish, new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }
        else if (subscribe.isDebug()) {
            Logger.debug("received PUBLISH: (Topic: '{}', MESSAGE: '{}')", mqtt5Publish.getTopic(), new String(mqtt5Publish.getPayloadAsBytes(), StandardCharsets.UTF_8));
        }

    }


}
