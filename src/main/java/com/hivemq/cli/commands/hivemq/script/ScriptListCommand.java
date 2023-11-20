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

package com.hivemq.cli.commands.hivemq.script;

import com.hivemq.cli.MqttCLIMain;
import com.hivemq.cli.commands.hivemq.datahub.DataHubOptions;
import com.hivemq.cli.commands.hivemq.datahub.OutputFormatter;
import com.hivemq.cli.converters.ScriptTypeConverter;
import com.hivemq.cli.hivemq.scripts.ListScriptsTask;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "list",
                     description = "List all existing scripts",
                     sortOptions = false,
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class ScriptListCommand implements Callable<Integer> {

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-t", "--type"},
                        converter = ScriptTypeConverter.class,
                        description = "Filter by script type")
    private @Nullable String @Nullable [] functionTypes;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-i", "--id"}, description = "Filter by script id")
    private @Nullable String @Nullable [] scriptIds;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"-f", "--field"},
                        description = "Filter which JSON fields are included in the response")
    private @Nullable String @Nullable [] fields;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--limit"}, description = "Limit the number of returned scripts")
    private @Nullable Integer limit;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public ScriptListCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() {
        Logger.trace("Command {}", this);

        final DataHubScriptsApi scriptsApi =
                hiveMQRestService.getScriptsApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        if (limit != null && limit < 0) {
            outputFormatter.printError("The limit must not be negative.");
            return 1;
        }

        final ListScriptsTask listScriptsTask =
                new ListScriptsTask(outputFormatter, scriptsApi, functionTypes, scriptIds, fields, limit);

        if (listScriptsTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "ListScriptsCommand{" +
                "functionTypes=" + Arrays.toString(functionTypes) +
                ", scriptIds=" + Arrays.toString(scriptIds) +
                ", fields=" + Arrays.toString(fields) +
                ", limit=" + limit +
                ", dataHubOptions=" + dataHubOptions +
                ", outputFormatter=" + outputFormatter +
                ", hiveMQRestService=" + hiveMQRestService +
                '}';
    }

}
