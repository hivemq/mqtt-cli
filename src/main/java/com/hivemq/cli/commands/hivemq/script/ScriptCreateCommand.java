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
import com.hivemq.cli.commands.hivemq.datahub.ScriptDefinitionOptions;
import com.hivemq.cli.converters.ScriptTypeConverter;
import com.hivemq.cli.hivemq.scripts.CreateScriptTask;
import com.hivemq.cli.openapi.hivemq.DataHubScriptsApi;
import com.hivemq.cli.rest.HiveMQRestService;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create",
                     description = "Create a new script",
                     synopsisHeading = "%n@|bold Usage:|@  ",
                     descriptionHeading = "%n",
                     optionListHeading = "%n@|bold Options:|@%n",
                     commandListHeading = "%n@|bold Commands:|@%n",
                     versionProvider = MqttCLIMain.CLIVersionProvider.class,
                     mixinStandardHelpOptions = true)
public class ScriptCreateCommand implements Callable<Integer> {

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The id of the script")
    private @NotNull String scriptId;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"--type"},
                        required = true,
                        converter = ScriptTypeConverter.class,
                        description = "The function type")
    private @NotNull String functionType;

    @SuppressWarnings("unused")
    @CommandLine.Option(names = {"--print-version"},
                        defaultValue = "false",
                        description = "Print the assigned script version after successful creation.")
    private boolean printVersion;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.ArgGroup(multiplicity = "1")
    private @NotNull ScriptDefinitionOptions definitionOptions;

    @SuppressWarnings({"NotNullFieldNotInitialized", "unused"})
    @CommandLine.Option(names = {"--description"}, required = false, description = "The description of the script")
    private @NotNull String description;

    @CommandLine.Mixin
    private final @NotNull DataHubOptions dataHubOptions = new DataHubOptions();

    private final @NotNull OutputFormatter outputFormatter;
    private final @NotNull HiveMQRestService hiveMQRestService;

    @Inject
    public ScriptCreateCommand(
            final @NotNull HiveMQRestService hiveMQRestService, final @NotNull OutputFormatter outputFormatter) {
        this.outputFormatter = outputFormatter;
        this.hiveMQRestService = hiveMQRestService;
    }

    @Override
    public @NotNull Integer call() throws IOException {
        Logger.trace("Command {}", this);

        final DataHubScriptsApi scriptsApi =
                hiveMQRestService.getScriptsApi(dataHubOptions.getUrl(), dataHubOptions.getRateLimit());

        final String fileDefinition = definitionOptions.getFile();
        final ByteBuffer argumentDefinition = definitionOptions.getArgument();
        final ByteBuffer definitionBytes;
        if (fileDefinition != null) {
            try {
                final Path path = Paths.get(fileDefinition);
                definitionBytes = ByteBuffer.wrap(Files.readAllBytes(path));
            } catch (final IOException exception) {
                outputFormatter.printError("File not found or not readable: " + fileDefinition);
                return 1;
            }
        } else if (argumentDefinition != null) {
            definitionBytes = argumentDefinition;
        } else {
            throw new RuntimeException("One of --file or --definition must be set.");
        }

        final CreateScriptTask createScriptTask = new CreateScriptTask(outputFormatter,
                scriptsApi,
                scriptId,
                functionType,
                description,
                printVersion,
                definitionBytes);
        if (createScriptTask.execute()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public @NotNull String toString() {
        return "CreateScriptCommand{" +
                "scriptId='" + scriptId + '\'' +
                ", functionType='" + functionType + '\'' +
                ", printVersion=" + printVersion +
                ", definitionOptions=" + definitionOptions +
                ", description='" + description + '\'' +
                ", dataHubOptions=" + dataHubOptions +
                ", outputFormatter=" + outputFormatter +
                ", hiveMQRestService=" + hiveMQRestService +
                '}';
    }

}
