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

import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Arrays;
import java.util.Optional;

public class KillCliTestExecutionListener implements TestExecutionListener {

    @Override
    public void executionFinished(
            final @NotNull TestIdentifier testIdentifier,
            final @NotNull TestExecutionResult testExecutionResult) {
        final String processIds = System.getProperty(testIdentifier.getUniqueId());
        if (processIds == null) {
            return;
        }
        System.clearProperty(testIdentifier.getUniqueId());
        Arrays.stream(processIds.split(":")).forEach(processId -> {
            final Optional<ProcessHandle> childProcess = ProcessHandle.of(Long.parseLong(processId));
            childProcess.ifPresent(ProcessHandle::destroyForcibly);
        });
    }
}
