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
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class MqttPublishUtils {

    public static String formatPayload(final byte @NotNull [] payload, final boolean isBase64) {
        if (isBase64) {
            return Base64.toBase64String(payload);
        } else {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    public static void printToFile(final @NotNull File publishFile, final @NotNull String message) {
        // Re-create a deleted output file if it was deleted manually
        try {
            if (publishFile.createNewFile()) {
                Logger.debug("Re-created deleted output file {}", publishFile.getAbsolutePath());
            }
        } catch (final @NotNull IOException e) {
            Logger.error("Cannot re-create deleted output file {}", publishFile.getAbsolutePath(), e.getMessage());
            return;
        }

        try {
            Files.write(publishFile.toPath(),
                    (message + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
        } catch (final @NotNull IOException e) {
            Logger.error("Cannot write to output file {}", publishFile.getAbsolutePath(), e.getMessage());
        }
    }
}
