package com.hivemq.cli.commands.hivemq.policies;

import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.policies.DeletePolicyTask;
import com.hivemq.cli.openapi.hivemq.PoliciesApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "delete", description = "Delete an existing policy", mixinStandardHelpOptions = true)
public class DeletePolicyCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--id"}, description = "the id of the policy")
    private @Nullable String policyId;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter formatter;

    @Inject
    public DeletePolicyCommand(final @NotNull OutputFormatter outputFormatter) {
        this.formatter = outputFormatter;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final PoliciesApi policiesApi =
                HiveMQRestService.getPoliciesApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final DeletePolicyTask deletePolicyTask = new DeletePolicyTask(formatter, policiesApi, policyId);
        if (deletePolicyTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "DeletePolicyCommand{" +
                "policyId='" +
                policyId +
                '\'' +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", formatter=" +
                formatter +
                '}';
    }
}
