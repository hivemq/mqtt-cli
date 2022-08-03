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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CommandConsumer implements Consumer<String> {

    private final @NotNull Map<String, CompletableFuture<Void>> patterns = new ConcurrentHashMap<>();
    private final @NotNull Map<String, CompletableFuture<Void>> contains = new ConcurrentHashMap<>();

    @Override
    public void accept(final @NotNull String commandLine) {
        patterns.forEach((pattern, future) -> {
            if (commandLine.trim().matches(pattern)) {
                future.complete(null);
            }
        });
        contains.forEach((command, future) -> {
            if (commandLine.contains(command)) {
                future.complete(null);
            }
        });
    }

    public @NotNull CompletableFuture<Void> waitForPattern(final @NotNull String pattern) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        patterns.put(pattern, future);
        return future;
    }

    public @NotNull CompletableFuture<Void> waitFor(final @NotNull String command) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        contains.put(command, future);
        return future;
    }
}