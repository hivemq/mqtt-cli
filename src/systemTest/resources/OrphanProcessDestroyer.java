package com.hivemq.cli.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessKiller {

    public static void main(final String[] args)
            throws ExecutionException, InterruptedException, TimeoutException {
        final String jvmProcessId = args[0];
        final String childProcessId = args[1];
        final ProcessHandle jvmProcess = ProcessHandle.of(Long.parseLong(jvmProcessId)).get();
        final ProcessHandle childProcess = ProcessHandle.of(Long.parseLong(childProcessId)).get();
        final CompletableFuture<ProcessHandle> future =
                jvmProcess.onExit().whenComplete((processHandle, throwable) -> {
                    childProcess.destroyForcibly();
                    System.exit(0);
                });
        System.out.println('X');
        future.get(300, TimeUnit.SECONDS);
    }
}

