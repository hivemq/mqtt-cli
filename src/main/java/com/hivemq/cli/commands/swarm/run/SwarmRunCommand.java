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

package com.hivemq.cli.commands.swarm.run;

import com.hivemq.cli.MqttCLIMain;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "run",
                     description = "HiveMQ Swarm Run Command Line Interpreter.",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class SwarmRunCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Spec
    private @NotNull CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    private final @NotNull SwarmOptions swarmOptions = new SwarmOptions();

    @Inject
    public SwarmRunCommand() {
    }

    @Override
    public @NotNull Integer call() throws Exception {
        System.out.println(spec.commandLine().getUsageMessage(spec.commandLine().getColorScheme()));
        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "SwarmRunCommand{" + "spec=" + spec + ", swarmOptions=" + swarmOptions + '}';
    }
}
