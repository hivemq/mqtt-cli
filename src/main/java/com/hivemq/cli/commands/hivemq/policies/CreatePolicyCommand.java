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

import com.google.gson.Gson;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.commands.hivemq.datagovernance.PolicyDefinitionOptions;
import com.hivemq.cli.hivemq.policies.CreatePolicyTask;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
                     description = "Create a new policy",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class CreatePolicyCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull PolicyDefinitionOptions definitionOptions;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;
    private final @NotNull Gson gson;

    @Inject
    public CreatePolicyCommand(
            final @NotNull HiveMQRestService hiveMQRestService,
            final @NotNull OutputFormatter outputFormatter,
            final @NotNull Gson gson) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
        this.gson = gson;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final PoliciesApi policiesApi =
                hiveMQRestService.getPoliciesApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final String definition = definitionOptions.getDefinition();
        if (definition.isEmpty()) {
            outputFormatter.printError("The policy definition must not be empty.");
            return 1;
        }

        final CreatePolicyTask createPolicyTask = new CreatePolicyTask(outputFormatter, policiesApi, gson, definition);
        if (createPolicyTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "CreatePolicyCommand{" +
                "definitionOptions=" +
                definitionOptions +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", outputFormatter=" +
                outputFormatter +
                '}';
    }
}
