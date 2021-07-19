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
package com.hivemq.cli.utils;

import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.PrintWriter;

public class MqttPublishUtils {

    public static String formatPayload(final byte[] payload, final boolean isBase64) {
        if (isBase64) {
            return Base64.toBase64String(payload);
        }
        else {
            return new String(payload);
        }
    }

    public static void printToFile(final @NotNull File publishFile, final @NotNull String message) {
        final PrintWriter fileWriter = FileUtils.createFileAppender(publishFile);
        fileWriter.println(message);
        fileWriter.flush();
        fileWriter.close();
    }

}
