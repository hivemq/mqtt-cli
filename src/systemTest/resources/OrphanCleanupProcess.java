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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OrphanCleanupProcess {

    public static void main(final String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        final String jvmProcessId = args[0];
        final String childProcessId = args[1];
        final ProcessHandle jvmProcess = ProcessHandle.of(Long.parseLong(jvmProcessId)).get();
        final ProcessHandle childProcess = ProcessHandle.of(Long.parseLong(childProcessId)).get();
        final CompletableFuture<ProcessHandle> future = jvmProcess.onExit().whenComplete((processHandle, throwable) -> {
            childProcess.destroyForcibly();
        });
        System.out.println('X');
        future.get(300, TimeUnit.SECONDS);
    }
}