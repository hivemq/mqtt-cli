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
package com.hivemq.cli.graal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hivemq.cli.utils.json.JsonMqttPublish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class NativeMain {

    private static final @NotNull Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();

    /**
     * This
     *
     * @param args
     */
    public static void main(final @NotNull String... args) {
        Logger.info("Hello World!");
        final Mqtt3Publish sdfsd = Mqtt3Publish.builder().topic("sdfsd").build();
        final JsonMqttPublish jsonMqttPublish = new JsonMqttPublish(sdfsd, true);
        Logger.info(gson.toJson(jsonMqttPublish));
    }
}
