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

package com.hivemq.cli.utils.cli.io;

import com.hivemq.cli.utils.exceptions.TimeoutException;
import org.awaitility.core.ConditionTimeoutException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogWaiter {

    private final @NotNull File logFile;
    private final @NotNull AtomicLong filePosition;

    public LogWaiter(final @NotNull File logFile) {
        this.logFile = logFile;
        this.filePosition = new AtomicLong(0);
    }

    public void awaitLog(final @NotNull String expectedLogMessage) throws TimeoutException {
        final AtomicReference<String> readLog = new AtomicReference<>();

        try {
            await().until(() -> {
                final StringBuilder readChars = new StringBuilder();
                try {
                    try (final FileReader fileReader = new FileReader(logFile, StandardCharsets.UTF_8)) {
                        assertEquals(filePosition.get(), fileReader.skip(filePosition.get()));
                        while (true) {
                            for (int i = 0; i < expectedLogMessage.length(); i++) {
                                final int readChar = fileReader.read();
                                readChars.append((char) readChar);

                                if (readChar == -1) {
                                    return false;
                                }

                                if (expectedLogMessage.charAt(i) != readChar) {
                                    break;
                                }


                                if (i == expectedLogMessage.length() - 1) {
                                    filePosition.set(filePosition.get() + readChars.length());
                                    return true;
                                }
                            }
                        }
                    }
                } finally {
                    readLog.set(readChars.toString());
                }
            });
        } catch (final ConditionTimeoutException e) {
            throw new TimeoutException(e, readLog.get());
        }
    }
}
