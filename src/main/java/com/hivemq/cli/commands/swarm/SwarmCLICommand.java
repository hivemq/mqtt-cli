package com.hivemq.cli.commands.swarm;

import com.hivemq.cli.MqttCLIMain;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * @author Yannick Weber
 */
@CommandLine.Command(name = "swarm",
        description = "HiveMQ Swarm Command Line Interpreter.",
        synopsisHeading = "%n@|bold Usage:|@  ",
        descriptionHeading = "%n",
        optionListHeading = "%n@|bold Options:|@%n",
        commandListHeading = "%n@|bold Commands:|@%n",
        mixinStandardHelpOptions = true,
        versionProvider = MqttCLIMain.CLIVersionProvider.class)
public class SwarmCLICommand implements Callable<Integer> {

    @CommandLine.Spec
    @Nullable CommandLine.Model.CommandSpec spec;

    @Inject
    public SwarmCLICommand() { }

    @Override
    public Integer call() {
        System.out.println(spec.commandLine().getUsageMessage(spec.commandLine().getColorScheme()));
        return 0;
    }

}
