package com.hivemq.cli.commands.hivemq.schemas;

import com.hivemq.cli.commands.hivemq.datagovernance.DataGovernanceOptions;
import com.hivemq.cli.commands.hivemq.datagovernance.OutputFormatter;
import com.hivemq.cli.hivemq.schemas.GetSchemaTask;
import com.hivemq.cli.openapi.hivemq.SchemasApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "get", description = "Get an existing schema", mixinStandardHelpOptions = true)
public class GetSchemaCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "the id of the schema")
    private @NotNull String schemaId;

    @CommandLine.Mixin
    private @NotNull DataGovernanceOptions dataGovernanceOptions = new DataGovernanceOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public GetSchemaCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @VisibleForTesting
    public GetSchemaCommand(
            final @NotNull String url,
            final int rateLimit,
            final @NotNull String schemaId,
            final @NotNull HiveMQRestService hiveMQRestService,
            final @NotNull OutputFormatter outputFormatter) {
        this.schemaId = schemaId;
        this.dataGovernanceOptions = new DataGovernanceOptions(url, rateLimit);
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final SchemasApi schemasApi =
                hiveMQRestService.getSchemasApi(dataGovernanceOptions.getUrl(), dataGovernanceOptions.getRateLimit());

        final GetSchemaTask getSchemaTask = new GetSchemaTask(outputFormatter, schemasApi, schemaId);
        if (getSchemaTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "GetSchemaCommand{" +
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
