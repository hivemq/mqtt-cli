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

import com.google.common.io.Resources;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrphanProcessCleanup {

    public static @NotNull Process startOrphanCleanupProcess(final @NotNull Process childProcess) throws IOException {
        // We start the OrphanCleanupProcess which sole job is to destroy the childProcess, meaning the mqtt-cli shell,
        // when the jvm process exited
        final long jvmProcessId = ProcessHandle.current().pid();
        final List<String> orphanCleanupProcessCommand = List.of(
                System.getProperty("java"),
                Resources.getResource("OrphanCleanupProcess.java").getPath(),
                String.valueOf(jvmProcessId),
                String.valueOf(childProcess.pid()));
        final Process orphanCleanupProcess = new ProcessBuilder(orphanCleanupProcessCommand).start();

        // Wait until the process prints X, which means that the orphan cleanup process has successfully started
        final int readChar = orphanCleanupProcess.getInputStream().read();
        assertEquals('X', readChar);

        return orphanCleanupProcess;
    }
}
