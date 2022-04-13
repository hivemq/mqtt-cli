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

package com.hivemq.cli.commands.swarm.run;

import com.hivemq.cli.commands.swarm.error.Error;
import com.hivemq.cli.commands.swarm.error.SwarmApiErrorTransformer;
import com.hivemq.cli.openapi.ApiException;
import com.hivemq.cli.openapi.swarm.RunsApi;
import com.hivemq.cli.openapi.swarm.ScenariosApi;
import com.hivemq.cli.openapi.swarm.StopRunRequest;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

public class SwarmRunStartCommandShutdownHook extends Thread {

    private final @NotNull RunsApi runsApi;
    private final @NotNull ScenariosApi scenariosApi;
    private final @NotNull SwarmApiErrorTransformer errorTransformer;
    private final @NotNull Integer runId;
    private final @NotNull Integer scenarioId;

    public SwarmRunStartCommandShutdownHook(
            final @NotNull RunsApi runsApi,
            final @NotNull ScenariosApi scenariosApi,
            final @NotNull SwarmApiErrorTransformer errorTransformer,
            final @NotNull Integer runId,
            final @NotNull Integer scenarioId) {
        this.runsApi = runsApi;
        this.scenariosApi = scenariosApi;
        this.errorTransformer = errorTransformer;
        this.runId = runId;
        this.scenarioId = scenarioId;
    }

    @Override
    public void run() {
        try {
            final StopRunRequest stopRunRequest = new StopRunRequest();
            stopRunRequest.runStatus("STOPPING");
            runsApi.stopRun(runId.toString(), stopRunRequest);
        } catch (final ApiException e) {
            // catch this silently. Sometimes the run is already stopped when the shutdown hook is executed.
        }
        try {
            scenariosApi.deleteScenario(scenarioId.toString());
        } catch (final ApiException e) {
            final Error error = errorTransformer.transformError(e);
            Logger.error("Failed to delete scenario. {}.", error.getDetail());
        }
    }
}
