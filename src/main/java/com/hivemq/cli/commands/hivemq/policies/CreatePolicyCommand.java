package com.hivemq.cli.commands.hivemq.policies;

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

@CommandLine.Command(name = "create", description = "Create a new policy", mixinStandardHelpOptions = true)
public class CreatePolicyCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull PolicyDefinitionOptions definitionOptions;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;

    @Inject
    public CreatePolicyCommand(final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final PoliciesApi policiesApi =
                HiveMQRestService.getPoliciesApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final String definition = definitionOptions.getDefinition();
        if (definition.isEmpty()) {
            outputFormatter.printError("The option '--definition' must not be empty.");
            return 1;
        }

        final CreatePolicyTask createPolicyTask = new CreatePolicyTask(outputFormatter, policiesApi, definition);
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
