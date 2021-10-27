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
package com.hivemq.cli.commands;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public interface Subscribe extends Context {

    @NotNull String[] getTopics();

    @NotNull MqttQos[] getQos();

    @Nullable File getOutputFile();

    boolean isPrintToSTDOUT();

    boolean isBase64();

    boolean isJsonOutput();

    boolean showTopics();

    @Nullable Mqtt5UserProperties getUserProperties();

    default boolean createOutputFile(final @Nullable File outputFile) {

        if (outputFile == null) {
            // option --outputToFile was not used
            return true;
        }

        if (outputFile.isDirectory()) {
            Logger.error("Cannot create output file {} as it is a directory", outputFile.getAbsolutePath());
            return false;
        }

        try {
            if (!outputFile.createNewFile()) { // This is only false if the file already exists
                Logger.debug("Writing to existing output file {}", outputFile.getAbsolutePath());
            }
        } catch (final @NotNull IOException e) {
            Logger.error("Could not create output file {}", outputFile.getAbsolutePath(), e);
            return false;
        }

        return true;

    }

}
