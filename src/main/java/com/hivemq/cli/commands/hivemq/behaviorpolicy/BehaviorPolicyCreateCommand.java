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

import com.google.gson.Gson;
import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datahub.DataHubOptions;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.commands.hivemq.datahub.PolicyDefinitionOptions;
import com.hivemq.cli.hivemq.behaviorpolicy.BehaviorPolicyCreateTask;
import com.hivemq.cli.openapi.hivemq.DataHubBehaviorPoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
                     description = "Create a new behavior policy",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class BehaviorPolicyCreateCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull PolicyDefinitionOptions definitionOptions;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;
    private final @NotNull Gson gson;

    @Inject
    public BehaviorPolicyCreateCommand(
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

        final DataHubBehaviorPoliciesApi behaviorPoliciesApi =
                hiveMQRestService.getBehaviorPoliciesApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        final String definition = definitionOptions.getDefinition();
        if (definition.isEmpty()) {
            outputFormatter.printError("The policy definition must not be empty.");
            return 1;
        }

        final BehaviorPolicyCreateTask behaviorPolicyCreateTask =
                new BehaviorPolicyCreateTask(outputFormatter, behaviorPoliciesApi, gson, definition);
        if (behaviorPolicyCreateTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "CreateBehaviorPolicyCommand{" +
                "definitionOptions=" +
                definitionOptions +
                ", dataHubOptions=" +
                dataHubOptions +
                ", outputFormatter=" +
                outputFormatter +
                ", hiveMQRestService=" +
                hiveMQRestService +
                ", gson=" +
                gson +
                '}';
    }
}
