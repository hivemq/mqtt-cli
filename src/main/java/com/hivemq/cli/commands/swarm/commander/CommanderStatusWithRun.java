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

package com.hivemq.cli.commands.swarm.commander;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommanderStatusWithRun extends CommanderStatus {

    private final @Nullable String runId;
    private final @Nullable Integer scenarioId;
    private final @Nullable String scenarioName;
    private final @Nullable String scenarioType;
    private final @Nullable String scenarioDescription;
    private final @Nullable String runStatus;
    private final @Nullable String scenarioStage;

    public CommanderStatusWithRun(
            final @NotNull String status,
            final @Nullable String runId,
            final @Nullable Integer scenarioId,
            final @Nullable String scenarioName,
            final @Nullable String scenarioType,
            final @Nullable String scenarioDescription,
            final @Nullable String runStatus,
            final @Nullable String scenarioStage) {
        super(status);
        this.runId = runId;
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.scenarioDescription = scenarioDescription;
        this.runStatus = runStatus;
        this.scenarioStage = scenarioStage;
    }

    public @Nullable String getRunId() {
        return runId;
    }

    public @Nullable Integer getScenarioId() {
        return scenarioId;
    }

    public @Nullable String getScenarioName() {
        return scenarioName;
    }

    public @Nullable String getScenarioType() {
        return scenarioType;
    }

    public @Nullable String getRunStatus() {
        return runStatus;
    }

    public @Nullable String getScenarioStage() {
        return scenarioStage;
    }

    public @Nullable String getScenarioDescription() {
        return scenarioDescription;
    }
}
