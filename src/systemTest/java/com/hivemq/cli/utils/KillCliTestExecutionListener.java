package com.hivemq.cli.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Optional;

public class KillCliTestExecutionListener implements TestExecutionListener {

    @Override
    public void executionFinished(
            final @NotNull TestIdentifier testIdentifier, final @NotNull TestExecutionResult testExecutionResult) {
        final String processId = System.getProperty(testIdentifier.getUniqueId());
        if (processId == null) {
            return;
        }
        System.clearProperty(testIdentifier.getUniqueId());
        final Optional<ProcessHandle> childProcess = ProcessHandle.of(Long.parseLong(processId));
        childProcess.ifPresent(ProcessHandle::destroyForcibly);
    }
}
