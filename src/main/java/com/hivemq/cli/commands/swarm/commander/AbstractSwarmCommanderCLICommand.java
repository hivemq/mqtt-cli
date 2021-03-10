package com.hivemq.cli.commands.swarm.commander;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.AbstractSwarmCommand;
import com.hivemq.cli.openapi.ApiClient;
import com.hivemq.cli.openapi.swarm.CommanderApi;
import com.hivemq.cli.openapi.swarm.CommanderStateResponse;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "commander",
        description = "HiveMQ Swarm Commander Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public abstract class AbstractSwarmCommanderCLICommand extends AbstractSwarmCommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-j", "--json"}, defaultValue = "false", description = "Toggle json output.", order = 4)
    protected @NotNull Boolean json;
}
