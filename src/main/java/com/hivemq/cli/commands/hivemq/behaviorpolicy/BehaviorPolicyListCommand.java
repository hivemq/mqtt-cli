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

package com.hivemq.cli.commands.hivemq.behaviorpolicy;

import com.hivemq.cli.commands.hivemq.datahub.DataHubOptions;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.hivemq.behaviorpolicy.BehaviorPolicyListTask;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list",
                     description = "List all existing behavior policies",
                     mixinStandardHelpOptions = true)
public class BehaviorPolicyListCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--topic"}, description = "List only policies that match a topic")
    private @Nullable String topic;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--id"}, description = "Filter by policy id", paramLabel = "<policyId>")
    private @Nullable String @Nullable [] policyIds;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-c", "--client-id"},
                        description = "Filter by policies matching a client id",
                        paramLabel = "<clientId>")
    private @Nullable String @Nullable [] clientIds;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-f", "--field"},
                        description = "Filter which JSON fields are included in the response",
                        paramLabel = "<field>")
    private @Nullable String @Nullable [] fields;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--limit"}, description = "Limit the number of returned policies")
    private @Nullable Integer limit;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public BehaviorPolicyListCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final DataHubBehaviorPoliciesApi behaviorPoliciesApi =
                hiveMQRestService.getBehaviorPoliciesApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        if (limit != null && limit < 0) {
            outputFormatter.printError("The limit must not be negative.");
            return 1;
        }

        final BehaviorPolicyListTask behaviorPolicyListTask =
                new BehaviorPolicyListTask(outputFormatter, behaviorPoliciesApi, policyIds, clientIds, fields, limit);

        if (behaviorPolicyListTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "ListBehaviorPoliciesCommand{" +
                "topic='" +
                topic +
                '\'' +
                ", policyIds=" +
                Arrays.toString(policyIds) +
                ", clientIds=" +
                Arrays.toString(clientIds) +
                ", fields=" +
                Arrays.toString(fields) +
                ", limit=" +
                limit +
                ", dataHubOptions=" +
                dataHubOptions +
                ", outputFormatter=" +
                outputFormatter +
                ", hiveMQRestService=" +
                hiveMQRestService +
                '}';
    }
}
