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

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.policies.GetPolicyTask;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "get",
                     description = "Get a policy",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class GetPolicyCommand implements Callable<Integer> {

    @SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The id of the policy")
    private @NotNull String policyId;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-f", "--field"},
                        description = "Filter which JSON fields are included in the response")
    private @Nullable String @Nullable [] fields;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public GetPolicyCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final PoliciesApi policiesApi =
                hiveMQRestService.getPoliciesApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        if (policyId.isEmpty()) {
            outputFormatter.printError("The policy id must not be empty.");
            return 1;
        }

        final GetPolicyTask getPolicyTask = new GetPolicyTask(outputFormatter, policiesApi, policyId, fields);
        if (getPolicyTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "GetPolicyCommand{" +
                "policyId='" +
                policyId +
                '\'' +
                ", fields=" +
                Arrays.toString(fields) +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", outputFormatter=" +
                outputFormatter +
                ", hiveMQRestService=" +
                hiveMQRestService +
                '}';
    }
}
