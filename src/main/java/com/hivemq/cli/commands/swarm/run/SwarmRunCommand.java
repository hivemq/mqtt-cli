package com.hivemq.cli.commands.swarm.run;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.swarm.AbstractSwarmCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "run",
        description = "HiveMQ Swarm Run Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class SwarmRunCommand extends AbstractSwarmCommand {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    public SwarmRunCommand() { }

    @Override
    public @NotNull Integer call() throws Exception {
        System.out.println(spec.commandLine().getUsageMessage(spec.commandLine().getColorScheme()));
        return 0;
    }
}
