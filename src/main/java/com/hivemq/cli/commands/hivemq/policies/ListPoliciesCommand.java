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

package com.hivemq.cli.commands.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.policies.ListPoliciesTask;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list", description = "List all existing policies", mixinStandardHelpOptions = true)
public class ListPoliciesCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-t", "--topic"}, description = "List only policies that match a topic")
    private @Nullable String topic;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--id"}, description = "Filter by policy id")
    private @Nullable String @Nullable [] policyIds;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-s", "--schema-id"}, description = "Filter by policies containing a schema id")
    private @Nullable String @Nullable [] schemaIds;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public ListPoliciesCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final PoliciesApi policiesApi =
                hiveMQRestService.getPoliciesApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final ListPoliciesTask listPoliciesTask =
                new ListPoliciesTask(outputFormatter, policiesApi, topic, policyIds, schemaIds);

        if (listPoliciesTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "ListPoliciesCommand{" +
                "topic='" +
                topic +
                '\'' +
                ", policyIds=" +
                Arrays.toString(policyIds) +
                ", schemaIds=" +
                Arrays.toString(schemaIds) +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", formatter=" +
                outputFormatter +
                '}';
    }
}
