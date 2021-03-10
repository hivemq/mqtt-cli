package com.hivemq.cli.commands.swarm.scenario;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.AbstractSwarmCommand;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "scenario",
        description = "HiveMQ Swarm Scenario Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public abstract class SwarmScenarioCLICommand extends AbstractSwarmCommand {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    public SwarmScenarioCLICommand() { }

}
