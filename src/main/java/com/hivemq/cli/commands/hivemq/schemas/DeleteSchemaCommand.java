package com.hivemq.cli.commands.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.schemas.DeleteSchemaTask;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "delete", description = "Delete an existing schema", mixinStandardHelpOptions = true)
public class DeleteSchemaCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "the schema id")
    private @NotNull String schemaId;

    @CommandLine.Mixin
    private final @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public DeleteSchemaCommand(
            final @NotNull HiveMQRestService hiveMQRestService,
            final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final SchemasApi schemasApi =
                hiveMQRestService.getSchemasApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final DeleteSchemaTask deleteSchemaTask = new DeleteSchemaTask(outputFormatter, schemasApi, schemaId);
        if (deleteSchemaTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "DeleteSchemaCommand{" +
                "schemaId='" +
                schemaId +
                '\'' +
                ", dataGovernanceOptions=" +
                dataGovernanceOptions +
                ", outputFormatter=" +
                outputFormatter +
                '}';
    }
}
