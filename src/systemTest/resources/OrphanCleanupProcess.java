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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OrphanCleanupProcess {

    public static void main(final String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        final String jvmProcessId = args[0];
        final String childProcessId = args[1];
        final Optional<ProcessHandle> jvmProcess = ProcessHandle.of(Long.parseLong(jvmProcessId));
        final Optional<ProcessHandle> childProcess = ProcessHandle.of(Long.parseLong(childProcessId));
        final CompletableFuture<ProcessHandle> future;
        if (jvmProcess.isPresent() && childProcess.isPresent()) {
            future = jvmProcess.get().onExit().whenComplete((processHandle, throwable) -> childProcess.get().destroyForcibly());
        } else if (jvmProcess.isEmpty() && childProcess.isPresent()) {
            childProcess.get().destroyForcibly();
            future = CompletableFuture.completedFuture(null);
        } else {
            future = CompletableFuture.completedFuture(null);
        }
        System.out.println('X');
        future.get(300, TimeUnit.SECONDS);
    }
}
